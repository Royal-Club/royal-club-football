package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.enums.MatchStatus;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.FixtureGenerationRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchFixtureUpdateRequest;
import com.bjit.royalclub.royalclubfootball.repository.MatchRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.*;


@Service
@RequiredArgsConstructor
public class FixtureGenerationServiceImpl implements FixtureGenerationService {

    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final VenueRepository venueRepository;

    @Override
    public List<Match> generateFixtures(Long tournamentId, FixtureGenerationRequest fixtureRequest) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check for duplicate fixtures
        if (matchRepository.hasScheduledFixtures(tournamentId)) {
            throw new TournamentServiceException(FIXTURES_ALREADY_EXIST + ". Use the clear fixtures endpoint first or contact administrator.", HttpStatus.CONFLICT);
        }

        // Get all teams in tournament
        List<Team> allTeams = teamRepository.findTeamsWithPlayersByTournamentId(tournamentId);

        if (allTeams.isEmpty()) {
            throw new TournamentServiceException("No teams found in tournament", HttpStatus.BAD_REQUEST);
        }

        if (allTeams.size() < 2) {
            throw new TournamentServiceException(INSUFFICIENT_TEAMS + ". Minimum 2 teams required.", HttpStatus.BAD_REQUEST);
        }

        // Get team IDs from request
        if (fixtureRequest.getTeamIds() == null || fixtureRequest.getTeamIds().isEmpty()) {
            throw new TournamentServiceException("Team IDs are required for fixture generation", HttpStatus.BAD_REQUEST);
        }

        // Get match dates from request
        if (fixtureRequest.getMatchDates() == null || fixtureRequest.getMatchDates().isEmpty()) {
            throw new TournamentServiceException("At least one match date is required", HttpStatus.BAD_REQUEST);
        }

        List<Match> matches = new ArrayList<>();
        Venue venue = fixtureRequest.getVenueId() != null ?
                venueRepository.findById(fixtureRequest.getVenueId()).orElse(null) :
                tournament.getVenue();

        // Get selected team IDs
        List<Long> teamIds = fixtureRequest.getTeamIds();
        List<Team> selectedTeams = allTeams.stream()
                .filter(t -> teamIds.contains(t.getId()))
                .collect(Collectors.toList());

        if (selectedTeams.isEmpty()) {
            throw new TournamentServiceException("None of the provided team IDs exist in tournament", HttpStatus.BAD_REQUEST);
        }

        if (selectedTeams.size() < 2) {
            throw new TournamentServiceException(INSUFFICIENT_TEAMS + ". At least 2 teams must be selected.", HttpStatus.BAD_REQUEST);
        }

        // Calculate scheduling parameters
        Integer timeGapMinutes = fixtureRequest.getTimeGapMinutes() != null ? fixtureRequest.getTimeGapMinutes() : 120;
        Integer matchDurationMinutes = fixtureRequest.getMatchDurationMinutes() != null ? fixtureRequest.getMatchDurationMinutes() : 90;

        // Calculate matches per day (assume 12-hour window per day)
        int minutesPerDay = 12 * 60;
        int matchesPerDay = minutesPerDay / timeGapMinutes;
        if (matchesPerDay < 1) {
            matchesPerDay = 1;
        }

        // Get start date - use first date from matchDates as base
        LocalDateTime startDate = fixtureRequest.getMatchDates().get(0);

        // Generate match combinations (round-robin style)
        int matchOrder = 1;
        int matchIndex = 0;

        for (int i = 0; i < selectedTeams.size(); i++) {
            for (int j = i + 1; j < selectedTeams.size(); j++) {
                // Calculate which day this match falls on
                int dayOffset = matchIndex / matchesPerDay;
                int matchIndexOnDay = matchIndex % matchesPerDay;

                // Calculate match date with proper time gap
                LocalDateTime matchDate = startDate
                        .plusDays(dayOffset)
                        .plusMinutes((long) matchIndexOnDay * timeGapMinutes);

                Match match = Match.builder()
                        .tournament(tournament)
                        .homeTeam(selectedTeams.get(i))
                        .awayTeam(selectedTeams.get(j))
                        .venue(venue)
                        .matchDate(matchDate)
                        .matchStatus(MatchStatus.SCHEDULED)
                        .matchOrder(matchOrder++)
                        .homeTeamScore(0)
                        .awayTeamScore(0)
                        .elapsedTimeSeconds(0)
                        .matchDurationMinutes(matchDurationMinutes)
                        .build();

                matches.add(match);
                matchIndex++;
            }
        }

        return matchRepository.saveAll(matches);
    }

    @Override
    public void clearFixtures(Long tournamentId) {
        // Only delete SCHEDULED matches to protect completed/ongoing matches
        List<Match> scheduledMatches = matchRepository.findByTournamentIdAndStatus(tournamentId, MatchStatus.SCHEDULED);

        if (scheduledMatches.isEmpty()) {
            throw new TournamentServiceException(NO_SCHEDULED_FIXTURES, HttpStatus.NOT_FOUND);
        }

        matchRepository.deleteAll(scheduledMatches);
    }

    @Override
    public Match updateMatchFixture(Long matchId, MatchFixtureUpdateRequest updateRequest) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new TournamentServiceException(MATCH_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Validate match status - can only update scheduled matches
        if (match.getMatchStatus() != MatchStatus.SCHEDULED) {
            throw new TournamentServiceException("Cannot update match fixture details once match has started", HttpStatus.BAD_REQUEST);
        }

        // Update match date if provided
        if (updateRequest.getMatchDate() != null) {
            match.setMatchDate(updateRequest.getMatchDate());
        }

        // Update venue if provided
        if (updateRequest.getVenueId() != null) {
            Venue venue = venueRepository.findById(updateRequest.getVenueId())
                    .orElseThrow(() -> new TournamentServiceException(VENUE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
            match.setVenue(venue);
        }

        return matchRepository.save(match);
    }

}
