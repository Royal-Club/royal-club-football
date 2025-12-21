package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipantPlayer;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse;
import com.bjit.royalclub.royalclubfootball.model.LatestTournamentWithParticipantsResponse;
import com.bjit.royalclub.royalclubfootball.model.LatestTournamentWithUserParticipantsResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerParticipationResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentParticipantRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.ALREADY_PARTICIPANT;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PARTICIPANT_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.UNAUTHORIZED;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInPlayer;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.isUserAuthorizedForSelf;
import static com.bjit.royalclub.royalclubfootball.util.StringUtils.normalizeString;

@Service
@RequiredArgsConstructor
public class TournamentParticipantServiceImpl implements TournamentParticipantService {

    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final TournamentService tournamentService;
    private final PlayerService playerService;
    private final TournamentParticipantPlayerRepository participantPlayerRepository;

    @Override
    public void saveOrUpdateTournamentParticipant(TournamentParticipantRequest tournamentParticipantRequest) {
        /*TODO("This is workable nice but need to develop this below code as standard")*/
        if (Boolean.TRUE.equals(!isUserAuthorizedForSelf(tournamentParticipantRequest.getPlayerId())) &&
                getLoggedInPlayer().getRoles().stream()
                        .noneMatch(role -> "ADMIN".equals(role.getName()))) {
            throw new SecurityException(UNAUTHORIZED);
        }
        Tournament tournament = getTournament(tournamentParticipantRequest.getTournamentId());
        Player player = getPlayer(tournamentParticipantRequest.getPlayerId());

        validateTournamentDate(tournament.getTournamentDate());

        TournamentParticipant tournamentParticipant = tournamentParticipantRequest.getTournamentParticipantId() != null
                ? getExistingParticipant(tournamentParticipantRequest.getTournamentParticipantId())
                : createNewParticipant(tournament, player);

        updateParticipantDetails(tournamentParticipant, tournament, player,
                tournamentParticipantRequest.isParticipationStatus(), tournamentParticipantRequest.getComments());
        tournamentParticipantRepository.save(tournamentParticipant);
    }

    private Tournament getTournament(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Player getPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private TournamentParticipant getExistingParticipant(Long participantId) {
        return tournamentParticipantRepository.findById(participantId)
                .orElseThrow(() -> new TournamentServiceException(PARTICIPANT_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private TournamentParticipant createNewParticipant(Tournament tournament, Player player) {
        if (tournamentParticipantRepository.existsByTournamentIdAndPlayerId(tournament.getId(), player.getId())) {
            throw new TournamentServiceException(ALREADY_PARTICIPANT, HttpStatus.CONFLICT);
        }
        return TournamentParticipant.builder()
                .createdDate(LocalDateTime.now())
                .build();
    }

    private void updateParticipantDetails(TournamentParticipant participant, Tournament tournament, Player player,
                                          boolean participationStatus, String newComments) {
        participant.setTournament(tournament);
        participant.setPlayer(player);
        participant.setParticipationStatus(participationStatus);
        participant.setComments(normalizeString(newComments));
    }

    private void validateTournamentDate(LocalDateTime tournamentDate) {
        if (tournamentDate.isBefore(LocalDateTime.now())) {
            throw new TournamentServiceException(TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<PlayerParticipationResponse> playersToBeSelectedForTeam(Long tournamentId) {
        return tournamentParticipantRepository.findAllByTournamentIdAndParticipationStatusTrue(tournamentId).stream()
                .filter(participant -> !isPlayerAssignedToAnyTeam(participant))
                .map(this::convertToPlayerParticipationResponse)
                .toList();
    }

    @Override
    public List<GoalkeeperStatsResponse> goalkeeperStatsResponse(Long tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        List<Long> teamIds = tournament.getTeams().stream().map(Team::getId).toList();
        List<Long> playerIds = tournamentParticipantRepository
                .findAllByTournamentIdAndParticipationStatusTrue(tournamentId).stream()
                .map(participant -> participant.getPlayer().getId())
                .toList();
        return teamPlayerRepository.findGoalkeeperStatsByPlayerIdsExcludingTeams(playerIds, teamIds);
    }

    private boolean isPlayerAssignedToAnyTeam(TournamentParticipant participant) {
        List<Long> teamIds = participant.getTournament().getTeams().stream()
                .map(Team::getId)
                .toList();
        return teamPlayerRepository.existsByTeamIdsAndPlayerId(teamIds, participant.getPlayer().getId());
    }

    private PlayerParticipationResponse convertToPlayerParticipationResponse(TournamentParticipant participant) {
        return PlayerParticipationResponse.builder()
                .playerId(participant.getPlayer().getId())
                .employeeId(participant.getPlayer().getEmployeeId())
                .playerName(participant.getPlayer().getName())
                .participationStatus(participant.isParticipationStatus())
                .comments(participant.getComments())
                .build();
    }

    @Override
    public LatestTournamentWithParticipantsResponse getLatestTournamentWithParticipants() {
        TournamentResponse latestTournament = tournamentService.getMostRecentTournament();

        int totalPlayers = playerService.countActivePlayers();

        int totalParticipants = tournamentParticipantRepository.countByTournamentIdAndParticipationStatusTrue(
                latestTournament.getId());

        return LatestTournamentWithParticipantsResponse.builder()
                .tournament(latestTournament)
                .totalParticipant(totalParticipants)
                .totalPlayer(totalPlayers)
                .remainParticipant(totalPlayers - totalParticipants)
                .build();
    }

    public LatestTournamentWithUserParticipantsResponse getLatestTournamentWithUserStatus() {
        TournamentResponse latestTournament = tournamentService.getMostRecentTournament();

        int totalPlayers = playerService.countActivePlayers();

        int totalParticipants = tournamentParticipantRepository.countByTournamentIdAndParticipationStatusTrue(
                latestTournament.getId());

        TournamentParticipantPlayer participantPlayer =
                participantPlayerRepository.findByTournamentIdAndPlayerId(latestTournament.getId(), getLoggedInPlayer().getId());

        // Handle null participantPlayer - user hasn't participated yet
        boolean isUserParticipated = participantPlayer != null && participantPlayer.getParticipationStatus();
        Long tournamentParticipantId = participantPlayer != null ? participantPlayer.getTournamentParticipantId() : null;

        return LatestTournamentWithUserParticipantsResponse.builder()
                .tournament(latestTournament)
                .totalParticipant(totalParticipants)
                .totalPlayer(totalPlayers)
                .remainParticipant(totalPlayers - totalParticipants)
                .isUserParticipated(isUserParticipated)
                .tournamentParticipantId(tournamentParticipantId)
                .build();
    }
}
