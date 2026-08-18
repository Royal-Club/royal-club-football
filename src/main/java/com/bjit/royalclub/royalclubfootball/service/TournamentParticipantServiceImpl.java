package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipantPlayer;
import com.bjit.royalclub.royalclubfootball.enums.ParticipationSource;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse;
import com.bjit.royalclub.royalclubfootball.model.LatestTournamentWithParticipantsResponse;
import com.bjit.royalclub.royalclubfootball.model.LatestTournamentWithUserParticipantsResponse;
import com.bjit.royalclub.royalclubfootball.model.PendingParticipantResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.ALREADY_PARTICIPANT;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PARTICIPANT_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.UNAUTHORIZED;
import static com.bjit.royalclub.royalclubfootball.enums.ParticipationSource.ADMIN;
import static com.bjit.royalclub.royalclubfootball.enums.ParticipationSource.SELF_APP;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInPlayer;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.isTournamentManager;
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
    private final TournamentVotingLockService votingLockService;

    @Override
    public void saveOrUpdateTournamentParticipant(TournamentParticipantRequest tournamentParticipantRequest) {
        boolean actingForSelf = Boolean.TRUE.equals(
                isUserAuthorizedForSelf(tournamentParticipantRequest.getPlayerId()));
        boolean manager = isTournamentManager();
        if (!actingForSelf && !manager) {
            throw new SecurityException(UNAUTHORIZED);
        }
        Tournament tournament = getTournament(tournamentParticipantRequest.getTournamentId());
        Player player = getPlayer(tournamentParticipantRequest.getPlayerId());

        validateTournamentDate(tournament.getTournamentDate());
        // Once the coordinator has closed the RSVP the squad is being picked from it, so only a
        // manager may still move an answer - on request from the member, who is told who to ask.
        votingLockService.requireVotingOpen(tournament);

        if (tournamentParticipantRequest.getParticipationStatus() == null) {
            clearParticipation(tournament, player);
            return;
        }

        TournamentParticipant tournamentParticipant = tournamentParticipantRequest.getTournamentParticipantId() != null
                ? getExistingParticipant(tournamentParticipantRequest.getTournamentParticipantId())
                : createNewParticipant(tournament, player);

        updateParticipantDetails(tournamentParticipant, tournament, player,
                tournamentParticipantRequest.getParticipationStatus(), tournamentParticipantRequest.getComments(),
                actingForSelf ? SELF_APP : ADMIN);
        tournamentParticipantRepository.save(tournamentParticipant);
    }

    /**
     * "Clear my answer" removes the row rather than storing a third state: every pending query in
     * the app - reminders, the pending list, the lock backfill - is expressed as the absence of a
     * row, so deleting is what actually puts the player back in the queue to be asked.
     */
    private void clearParticipation(Tournament tournament, Player player) {
        tournamentParticipantRepository.findByTournamentIdAndPlayerId(tournament.getId(), player.getId())
                .ifPresent(tournamentParticipantRepository::delete);
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
                                          boolean participationStatus, String newComments,
                                          ParticipationSource source) {
        participant.setTournament(tournament);
        participant.setPlayer(player);
        participant.setParticipationStatus(participationStatus);
        participant.setComments(normalizeString(newComments));
        // Overwriting the source matters on an auto-recorded No: once a manager has deliberately
        // set it, unlocking must leave it alone rather than reverting it to pending.
        participant.setParticipationSource(source);
    }

    private void validateTournamentDate(LocalDateTime tournamentDate) {
        if (tournamentDate.isBefore(LocalDateTime.now())) {
            throw new TournamentServiceException(TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<PlayerParticipationResponse> playersToBeSelectedForTeam(Long tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        List<Long> teamIds = tournament.getTeams().stream().map(Team::getId).toList();
        // One query for every already-assigned player instead of an exists check per participant.
        Set<Long> assignedPlayerIds = teamIds.isEmpty()
                ? Set.of()
                : new HashSet<>(teamPlayerRepository.findPlayerIdsByTeamIds(teamIds));
        return tournamentParticipantRepository.findAllByTournamentIdAndParticipationStatusTrueWithPlayer(tournamentId).stream()
                .filter(participant -> !assignedPlayerIds.contains(participant.getPlayer().getId()))
                .map(this::convertToPlayerParticipationResponse)
                .toList();
    }

    @Override
    public List<PendingParticipantResponse> playersPendingResponse(Long tournamentId) {
        // Ensure the tournament exists so callers get a clear 404 rather than an empty list.
        getTournament(tournamentId);
        return playerRepository.findActivePlayersWithoutParticipation(tournamentId).stream()
                .map(player -> PendingParticipantResponse.builder()
                        .playerId(player.getId())
                        .playerName(player.getName())
                        .employeeId(player.getEmployeeId())
                        .email(player.getEmail())
                        .mobileNo(player.getMobileNo())
                        .build())
                .toList();
    }

    @Override
    public List<GoalkeeperStatsResponse> goalkeeperStatsResponse(Long tournamentId) {
        Tournament tournament = getTournament(tournamentId);
        List<Long> teamIds = tournament.getTeams().stream().map(Team::getId).toList();
        List<Long> playerIds = tournamentParticipantRepository
                .findAllByTournamentIdAndParticipationStatusTrueWithPlayer(tournamentId).stream()
                .map(participant -> participant.getPlayer().getId())
                .toList();
        return teamPlayerRepository.findGoalkeeperStatsByPlayerIdsExcludingTeams(playerIds, teamIds);
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
        TournamentResponse latestTournament = tournamentService.getMostRecentActiveTournament();

        if (latestTournament == null) {
            return null;
        }

        int totalPlayers = playerService.countActivePlayers();

        int totalParticipants = tournamentParticipantRepository.countByTournamentIdAndParticipationStatusTrue(
                latestTournament.getId());

        TournamentParticipantPlayer participantPlayer =
                participantPlayerRepository.findByTournamentIdAndPlayerId(latestTournament.getId(), getLoggedInPlayer().getId());

        boolean isUserParticipated = participantPlayer != null &&
                participantPlayer.getParticipationStatus() != null &&
                participantPlayer.getParticipationStatus();
        Long tournamentParticipantId = participantPlayer != null ? participantPlayer.getTournamentParticipantId() : null;

        // Only worth a lookup once locked; while voting is open there is nobody to name.
        String lockedByName = latestTournament.isVotingLocked()
                ? votingLockService.lockedByName(getTournament(latestTournament.getId()))
                : null;

        // Read off the table rather than the tournament_participant_players view, which does not
        // carry the source. Only fetched when there is an answer to explain.
        ParticipationSource source = participantPlayer == null ? null
                : tournamentParticipantRepository
                        .findByTournamentIdAndPlayerId(latestTournament.getId(), getLoggedInPlayer().getId())
                        .map(TournamentParticipant::getParticipationSource)
                        .orElse(null);

        return LatestTournamentWithUserParticipantsResponse.builder()
                .tournament(latestTournament)
                .totalParticipant(totalParticipants)
                .totalPlayer(totalPlayers)
                .remainParticipant(totalPlayers - totalParticipants)
                .isUserParticipated(isUserParticipated)
                .tournamentParticipantId(tournamentParticipantId)
                .votingLockedByName(lockedByName)
                .participationSource(source)
                .build();
    }
}
