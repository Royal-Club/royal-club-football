package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TeamServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRemoveRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentTeamResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_ALREADY_ADDED_ANOTHER_TEAM;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_PARTICIPANT_YET;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_PART_OF_THIS_TEAM;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.enums.FootballPosition.getPositionOrDefault;

@Service
@RequiredArgsConstructor
public class TeamManagementServiceImpl implements TeamManagementService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final TournamentParticipantRepository tournamentParticipantRepository;

    @Override
    public TeamResponse createOrUpdateTeam(TeamRequest teamRequest) {
        Tournament tournament = validateAndGetTournament(teamRequest.getTournamentId());

        Team team = (teamRequest.getId() == null)
                ? createTeam(teamRequest, tournament)
                : updateTeam(teamRequest, tournament);

        teamRepository.save(team);
        return convertToTeamResponse(team);
    }

    @Override
    public void deleteTeam(Long teamId) {
        Team team = validateAndGetTeam(teamId);
        validateTournamentDate(team.getTournament());
        teamRepository.delete(team);
    }

    @Override
    public TeamPlayerResponse saveOrUpdateTeamPlayer(TeamPlayerRequest teamPlayerRequest) {
        Team team = validateAndGetTeam(teamPlayerRequest.getTeamId());
        validateTournamentDate(team.getTournament());
        Player player = validateAndGetPlayer(teamPlayerRequest.getPlayerId());

        TeamPlayer teamPlayer = (teamPlayerRequest.getId() == null)
                ? createTeamPlayer(teamPlayerRequest, team, player)
                : updateTeamPlayer(teamPlayerRequest, team, player);

        teamPlayerRepository.save(teamPlayer);
        return convertToTeamPlayerResponse(teamPlayer);
    }

    @Override
    public void removePlayerFromTeam(TeamPlayerRemoveRequest playerRemoveRequest) {
        validateAndGetTeam(playerRemoveRequest.getTeamId());
        validateAndGetPlayer(playerRemoveRequest.getPlayerId());
        TeamPlayer teamPlayer = teamPlayerRepository.findByTeamIdAndPlayerId(playerRemoveRequest.getTeamId(),
                playerRemoveRequest.getPlayerId()).orElseThrow(() ->
                new TournamentServiceException(PLAYER_IS_NOT_PART_OF_THIS_TEAM, HttpStatus.NOT_FOUND)
        );
        teamPlayerRepository.delete(teamPlayer);
    }

    @Override
    public List<TournamentResponse> getTournamentsSummery(Long tournamentId) {
        if (tournamentId != null) {
            return List.of(getTournamentWithTeamsAndPlayers(tournamentId));
        } else {
            return getAllTournamentsWithTeamsAndPlayers();
        }
    }

    private TournamentResponse getTournamentWithTeamsAndPlayers(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<Team> teams = teamRepository.findTeamsWithPlayersByTournamentId(tournamentId);

        List<TournamentTeamResponse> teamResponses = teams.stream()
                .map(this::convertToTournamentTeamResponse)
                .toList();

        return TournamentResponse.builder()
                .id(tournament.getId())
                .tournamentName(tournament.getName())
                .tournamentDate(tournament.getTournamentDate())
                .venueName(tournament.getVenue().getName())
                .activeStatus(tournament.isActive())
                .teams(teamResponses)
                .build();
    }

    private List<TournamentResponse> getAllTournamentsWithTeamsAndPlayers() {
        List<Tournament> tournaments = tournamentRepository.findAll();

        return tournaments.stream()
                .map(tournament -> {
                    List<Team> teams = teamRepository.findTeamsWithPlayersByTournamentId(tournament.getId());
                    List<TournamentTeamResponse> teamResponses = teams.stream()
                            .map(this::convertToTournamentTeamResponse)
                            .toList();
                    return TournamentResponse.builder()
                            .id(tournament.getId())
                            .tournamentName(tournament.getName())
                            .tournamentDate(tournament.getTournamentDate())
                            .venueName(tournament.getVenue().getName())
                            .activeStatus(tournament.isActive())
                            .teams(teamResponses)
                            .build();
                })
                .toList();
    }

    private TournamentTeamResponse convertToTournamentTeamResponse(Team team) {
        List<TeamPlayerResponse> players = team.getTeamPlayers().stream()
                .map(player -> TeamPlayerResponse.builder()
                        .id(player.getId())
                        .teamId(team.getId())
                        .playerId(player.getPlayer().getId())
                        .teamName(team.getTeamName())
                        .playerName(player.getPlayer().getName())
                        .playingPosition(player.getPlayingPosition())
                        .build())
                .toList();

        return TournamentTeamResponse.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .players(players)
                .build();
    }

    private boolean isPlayerAssignedToAnyTeamInTournament(Long tournamentId, Long playerId) {
        Tournament tournament = validateAndGetTournament(tournamentId);
        List<Long> teamIds = tournament.getTeams().stream().map(Team::getId).toList();
        return teamPlayerRepository.existsByTeamIdsAndPlayerId(teamIds, playerId);
    }

    private Tournament validateAndGetTournament(Long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Team validateAndGetTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamServiceException(TEAM_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private Player validateAndGetPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private void validateTournamentDate(Tournament tournament) {
        if (tournament.getTournamentDate().isBefore(LocalDateTime.now())) {
            throw new TournamentServiceException(TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE, HttpStatus.BAD_REQUEST);
        }
    }

    private Team createTeam(TeamRequest teamRequest, Tournament tournament) {
        return Team.builder()
                .teamName(teamRequest.getTeamName())
                .tournament(tournament)
                .createdDate(LocalDateTime.now())
                .build();
    }

    private Team updateTeam(TeamRequest teamRequest, Tournament tournament) {
        Team team = validateAndGetTeam(teamRequest.getId());
        team.setTeamName(teamRequest.getTeamName());
        team.setTournament(tournament);
        team.setUpdatedDate(LocalDateTime.now());
        return team;
    }

    private TeamPlayer createTeamPlayer(TeamPlayerRequest request, Team team, Player player) {

        boolean isParticipant = tournamentParticipantRepository
                .existsByTournamentIdAndPlayerIdAndParticipationStatusTrue(team.getTournament().getId(),
                        player.getId());

        if (!isParticipant) {
            throw new PlayerServiceException(PLAYER_IS_NOT_PARTICIPANT_YET, HttpStatus.BAD_REQUEST);
        }
        if (isPlayerAssignedToAnyTeamInTournament(team.getTournament().getId(), player.getId())) {
            throw new TeamServiceException(PLAYER_IS_ALREADY_ADDED_ANOTHER_TEAM, HttpStatus.CONFLICT);
        }
        return TeamPlayer.builder()
                .team(team)
                .player(player)
                .playingPosition(getPositionOrDefault(request.getPlayingPosition()))
                .createdDate(LocalDateTime.now())
                .build();
    }

    private TeamPlayer updateTeamPlayer(TeamPlayerRequest request, Team team, Player player) {
        TeamPlayer teamPlayer = teamPlayerRepository.findById(request.getId())
                .orElseThrow(() -> new TeamServiceException(TEAM_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        teamPlayer.setTeam(team);
        teamPlayer.setPlayer(player);
        teamPlayer.setPlayingPosition(getPositionOrDefault(request.getPlayingPosition()));
        teamPlayer.setUpdatedDate(LocalDateTime.now());
        return teamPlayer;
    }

    private TeamResponse convertToTeamResponse(Team team) {
        return TeamResponse.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .tournamentName(team.getTournament().getName())
                .tournamentId(team.getTournament().getId())
                .build();
    }

    private TeamPlayerResponse convertToTeamPlayerResponse(TeamPlayer teamPlayer) {
        return TeamPlayerResponse.builder()
                .id(teamPlayer.getId())
                .teamId(teamPlayer.getTeam().getId())
                .teamName(teamPlayer.getTeam().getTeamName())
                .playerId(teamPlayer.getPlayer().getId())
                .playerName(teamPlayer.getPlayer().getName())
                .playingPosition(teamPlayer.getPlayingPosition())
                .build();
    }
}
