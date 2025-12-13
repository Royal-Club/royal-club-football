package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.entity.MatchEvent;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.enums.MatchStatus;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.MatchEventRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchEventResponse;
import com.bjit.royalclub.royalclubfootball.model.MatchResponse;
import com.bjit.royalclub.royalclubfootball.model.MatchUpdateRequest;
import com.bjit.royalclub.royalclubfootball.repository.MatchEventRepository;
import com.bjit.royalclub.royalclubfootball.repository.MatchRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchManagementServiceImpl implements MatchManagementService {

    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final RoundGroupService roundGroupService;

    @Override
    public MatchResponse getMatchById(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        return convertToResponse(match);
    }

    @Override
    public MatchResponse startMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        if (!match.getMatchStatus().equals(MatchStatus.SCHEDULED)) {
            throw new TournamentServiceException("Only scheduled matches can be started", HttpStatus.CONFLICT);
        }

        match.setMatchStatus(MatchStatus.ONGOING);
        match.setStartedAt(LocalDateTime.now());
        match.setElapsedTimeSeconds(0);

        matchRepository.save(match);
        return convertToResponse(match);
    }

    @Override
    public MatchResponse pauseMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        if (!match.getMatchStatus().equals(MatchStatus.ONGOING)) {
            throw new TournamentServiceException("Only ongoing matches can be paused", HttpStatus.CONFLICT);
        }

        match.setMatchStatus(MatchStatus.PAUSED);
        matchRepository.save(match);
        return convertToResponse(match);
    }

    @Override
    public MatchResponse resumeMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        if (!match.getMatchStatus().equals(MatchStatus.PAUSED)) {
            throw new TournamentServiceException("Only paused matches can be resumed", HttpStatus.CONFLICT);
        }

        match.setMatchStatus(MatchStatus.ONGOING);
        matchRepository.save(match);
        return convertToResponse(match);
    }

    @Override
    public MatchResponse completeMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        if (!match.getMatchStatus().equals(MatchStatus.ONGOING)) {
            throw new TournamentServiceException("Only ongoing matches can be completed", HttpStatus.CONFLICT);
        }

        match.setMatchStatus(MatchStatus.COMPLETED);
        match.setCompletedAt(LocalDateTime.now());

        matchRepository.save(match);

        // Auto-update group standings if this match is part of a group
        if (match.getGroup() != null) {
            try {
                roundGroupService.recalculateGroupStandings(match.getGroup().getId());
            } catch (Exception e) {
                // Log error but don't fail the match completion
                // This is important to not break existing functionality
                System.err.println("Failed to auto-update group standings: " + e.getMessage());
            }
        }

        return convertToResponse(match);
    }

    @Override
    public MatchResponse updateMatch(Long matchId, MatchUpdateRequest updateRequest) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        if (updateRequest.getMatchStatus() != null) {
            match.setMatchStatus(MatchStatus.valueOf(updateRequest.getMatchStatus()));
        }

        if (updateRequest.getHomeTeamScore() != null) {
            match.setHomeTeamScore(updateRequest.getHomeTeamScore());
        }

        if (updateRequest.getAwayTeamScore() != null) {
            match.setAwayTeamScore(updateRequest.getAwayTeamScore());
        }

        if (updateRequest.getElapsedTimeSeconds() != null) {
            match.setElapsedTimeSeconds(updateRequest.getElapsedTimeSeconds());
        }

        matchRepository.save(match);
        return convertToResponse(match);
    }

    @Override
    public MatchEventResponse recordMatchEvent(MatchEventRequest eventRequest) {
        Match match = matchRepository.findById(eventRequest.getMatchId())
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        Player player = playerRepository.findById(eventRequest.getPlayerId())
                .orElseThrow(() -> new TournamentServiceException("Player not found", HttpStatus.NOT_FOUND));

        Team team = teamRepository.findById(eventRequest.getTeamId())
                .orElseThrow(() -> new TournamentServiceException("Team not found", HttpStatus.NOT_FOUND));

        Player relatedPlayer = null;
        if (eventRequest.getRelatedPlayerId() != null) {
            relatedPlayer = playerRepository.findById(eventRequest.getRelatedPlayerId())
                    .orElseThrow(() -> new TournamentServiceException("Related player not found", HttpStatus.NOT_FOUND));
        }

        MatchEvent matchEvent = MatchEvent.builder()
                .match(match)
                .eventType(eventRequest.getEventType())
                .player(player)
                .team(team)
                .eventTime(eventRequest.getEventTime())
                .description(eventRequest.getDescription())
                .relatedPlayer(relatedPlayer)
                .details(eventRequest.getDetails())
                .build();

        matchEventRepository.save(matchEvent);

        // Auto-update match score if it's a GOAL event
        if (eventRequest.getEventType().toString().equals("GOAL")) {
            if (match.getHomeTeam().getId().equals(eventRequest.getTeamId())) {
                match.setHomeTeamScore(match.getHomeTeamScore() + 1);
            } else if (match.getAwayTeam().getId().equals(eventRequest.getTeamId())) {
                match.setAwayTeamScore(match.getAwayTeamScore() + 1);
            }
            matchRepository.save(match);
        }

        return convertEventToResponse(matchEvent);
    }

    @Override
    public List<MatchEventResponse> getMatchEvents(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        return match.getMatchEvents().stream()
                .map(this::convertEventToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateElapsedTime(Long matchId, Integer elapsedSeconds) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        match.setElapsedTimeSeconds(elapsedSeconds);
        matchRepository.save(match);
    }

    @Override
    public void updateTeamScore(Long matchId, Long teamId, Integer newScore) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException("Match not found", HttpStatus.NOT_FOUND));

        if (match.getHomeTeam().getId().equals(teamId)) {
            match.setHomeTeamScore(newScore);
        } else if (match.getAwayTeam().getId().equals(teamId)) {
            match.setAwayTeamScore(newScore);
        } else {
            throw new TournamentServiceException("Team is not part of this match", HttpStatus.BAD_REQUEST);
        }

        matchRepository.save(match);
    }

    @Override
    public void deleteMatchEvent(Long eventId) {
        MatchEvent event = matchEventRepository.findById(eventId)
                .orElseThrow(() -> new TournamentServiceException("Match event not found", HttpStatus.NOT_FOUND));

        Match match = event.getMatch();

        // Validate match is still ongoing (not completed)
        if (match.getMatchStatus().equals(MatchStatus.COMPLETED)) {
            throw new TournamentServiceException("Cannot delete events from completed matches", HttpStatus.CONFLICT);
        }

        // If deleting a goal event, reverse the score
        if (event.getEventType().toString().equals("GOAL")) {
            if (match.getHomeTeam().getId().equals(event.getTeam().getId())) {
                if (match.getHomeTeamScore() > 0) {
                    match.setHomeTeamScore(match.getHomeTeamScore() - 1);
                }
            } else if (match.getAwayTeam().getId().equals(event.getTeam().getId())) {
                if (match.getAwayTeamScore() > 0) {
                    match.setAwayTeamScore(match.getAwayTeamScore() - 1);
                }
            }
            matchRepository.save(match);
        }

        // Delete the event
        matchEventRepository.delete(event);
    }

    private MatchResponse convertToResponse(Match match) {
        // Get roundNumber from TournamentRound if available, otherwise use legacyRound
        Integer roundNumber = null;
        if (match.getRound() != null) {
            roundNumber = match.getRound().getRoundNumber();
        } else if (match.getLegacyRound() != null) {
            roundNumber = match.getLegacyRound();
        }
        
        // Get groupName from group relationship if groupName column is null
        String groupName = match.getGroupName();
        if (groupName == null && match.getGroup() != null) {
            groupName = match.getGroup().getGroupName();
        }
        
        return MatchResponse.builder()
                .id(match.getId())
                .tournamentId(match.getTournament().getId())
                .tournamentName(match.getTournament().getName())
                .homeTeamId(match.getHomeTeam().getId())
                .homeTeamName(match.getHomeTeam().getTeamName())
                .awayTeamId(match.getAwayTeam().getId())
                .awayTeamName(match.getAwayTeam().getTeamName())
                .venueId(match.getVenue() != null ? match.getVenue().getId() : null)
                .venueName(match.getVenue() != null ? match.getVenue().getName() : null)
                .matchDate(match.getMatchDate())
                .matchStatus(match.getMatchStatus().toString())
                .matchOrder(match.getMatchOrder())
                .round(match.getLegacyRound())  // Keep legacy round for backward compatibility
                .roundNumber(roundNumber)  // Add roundNumber from TournamentRound or legacyRound
                .groupName(groupName)  // Use groupName from column or group relationship
                .homeTeamScore(match.getHomeTeamScore())
                .awayTeamScore(match.getAwayTeamScore())
                .matchDurationMinutes(match.getMatchDurationMinutes())
                .elapsedTimeSeconds(match.getElapsedTimeSeconds())
                .startedAt(match.getStartedAt())
                .completedAt(match.getCompletedAt())
                .createdDate(match.getCreatedDate())
                .updatedDate(match.getUpdatedDate())
                .build();
    }

    private MatchEventResponse convertEventToResponse(MatchEvent event) {
        return MatchEventResponse.builder()
                .id(event.getId())
                .matchId(event.getMatch().getId())
                .eventType(event.getEventType().toString())
                .playerId(event.getPlayer().getId())
                .playerName(event.getPlayer().getName())
                .teamId(event.getTeam().getId())
                .teamName(event.getTeam().getTeamName())
                .eventTime(event.getEventTime())
                .description(event.getDescription())
                .relatedPlayerId(event.getRelatedPlayer() != null ? event.getRelatedPlayer().getId() : null)
                .relatedPlayerName(event.getRelatedPlayer() != null ? event.getRelatedPlayer().getName() : null)
                .details(event.getDetails())
                .createdDate(event.getCreatedDate())
                .build();
    }

}
