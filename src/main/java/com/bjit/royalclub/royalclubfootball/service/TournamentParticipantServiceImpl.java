package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.PlayerParticipationResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentParticipantRequest;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PARTICIPANT_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TournamentParticipantServiceImpl implements TournamentParticipantService {

    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;

    @Override
    public void updateTournamentParticipant(TournamentParticipantRequest tournamentParticipantRequest) {
        Tournament tournament = tournamentRepository.findById(tournamentParticipantRequest.getTournamentId())
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        Player player = playerRepository.findById(tournamentParticipantRequest.getPlayerId())
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        validateTournamentDate(tournament.getTournamentDate());

        TournamentParticipant tournamentParticipant;
        if (tournamentParticipantRequest.getId() != null) {
            tournamentParticipant = tournamentParticipantRepository.findById(tournamentParticipantRequest.getId())
                    .orElseThrow(() -> new TournamentServiceException(PARTICIPANT_NOT_FOUND, HttpStatus.NOT_FOUND));
        } else {
            tournamentParticipant = new TournamentParticipant();
            tournamentParticipant.setCreatedDate(LocalDateTime.now());
        }

        tournamentParticipant.setTournament(tournament);
        tournamentParticipant.setPlayer(player);
        tournamentParticipant.setParticipationStatus(tournamentParticipantRequest.isParticipationStatus());
        tournamentParticipant.setUpdatedDate(LocalDateTime.now());
        tournamentParticipantRepository.save(tournamentParticipant);
    }

    private void validateTournamentDate(LocalDateTime tournamentDate) {
        if (tournamentDate.isBefore(LocalDateTime.now())) {
            throw new TournamentServiceException(TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<PlayerParticipationResponse> playersToBeSelectedForTeam(Long tournamentId) {
        List<TournamentParticipant> participants =
                tournamentParticipantRepository.findAllByTournamentIdAndParticipationStatusTrue(tournamentId);
        // Filter out participants who are already selected for a team
        List<TournamentParticipant> unselectedPlayers = participants.stream()
                .filter(participant -> !isPlayerAssignedToAnyTeam(participant))
                .toList();
        return unselectedPlayers.stream().map(this::convertToPlayerParticipationResponse).toList();
    }

    private boolean isPlayerAssignedToAnyTeam(TournamentParticipant participant) {
        Long playerId = participant.getPlayer().getId();
        List<Long> teamIds = participant.getTournament().getTeams().stream()
                .map(Team::getId)
                .toList();
        return teamPlayerRepository.existsByTeamIdsAndPlayerId(teamIds, playerId);
    }

    private PlayerParticipationResponse convertToPlayerParticipationResponse(TournamentParticipant participant) {
        return PlayerParticipationResponse.builder()
                .playerId(participant.getPlayer().getId())
                .employeeId(participant.getPlayer().getEmployeeId())
                .playerName(participant.getPlayer().getName())
                .participationStatus(participant.isParticipationStatus())
                .build();
    }
}
