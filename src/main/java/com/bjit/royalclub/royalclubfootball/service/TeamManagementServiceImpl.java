package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TeamServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
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

    @Override
    public TeamResponse createOrUpdateTeam(TeamRequest teamRequest) {
        Tournament tournament = validateAndGetTournament(teamRequest.getTournamentId());

        Team team = teamRequest.getId() == null
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

        TeamPlayer teamPlayer = teamPlayerRequest.getId() == null
                ? createTeamPlayer(teamPlayerRequest, team, player)
                : updateTeamPlayer(teamPlayerRequest, team, player);

        teamPlayerRepository.save(teamPlayer);
        return convertToTeamPlayerResponse(teamPlayer);
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
