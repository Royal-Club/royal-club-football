package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.enums.TeamPlayerRole;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.repository.TeamPlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamCaptainServiceImpl implements TeamCaptainService {

    private final TeamPlayerRepository teamPlayerRepository;
    private final TeamRepository teamRepository;

    @Override
    public TeamPlayerResponse setCaptain(Long teamId, Long playerId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new TournamentServiceException("Team not found", HttpStatus.NOT_FOUND));

        TeamPlayer teamPlayer = teamPlayerRepository.findByTeamIdAndPlayerId(teamId, playerId)
                .orElseThrow(() -> new TournamentServiceException("Player is not part of this team", HttpStatus.NOT_FOUND));

        // Remove any existing captain
        List<TeamPlayer> currentCaptains = teamPlayerRepository.findCaptainsByTeamId(teamId);
        for (TeamPlayer captain : currentCaptains) {
            if (captain.getTeamPlayerRole().equals(TeamPlayerRole.CAPTAIN)) {
                captain.setTeamPlayerRole(TeamPlayerRole.PLAYER);
                teamPlayerRepository.save(captain);
            }
        }

        teamPlayer.setTeamPlayerRole(TeamPlayerRole.CAPTAIN);
        teamPlayerRepository.save(teamPlayer);

        return convertToResponse(teamPlayer);
    }

    @Override
    public TeamPlayerResponse setViceCaptain(Long teamId, Long playerId) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new TournamentServiceException("Team not found", HttpStatus.NOT_FOUND));

        TeamPlayer teamPlayer = teamPlayerRepository.findByTeamIdAndPlayerId(teamId, playerId)
                .orElseThrow(() -> new TournamentServiceException("Player is not part of this team", HttpStatus.NOT_FOUND));

        teamPlayer.setTeamPlayerRole(TeamPlayerRole.VICE_CAPTAIN);
        teamPlayerRepository.save(teamPlayer);

        return convertToResponse(teamPlayer);
    }

    @Override
    public void removeCaptain(Long teamId, Long playerId) {
        TeamPlayer teamPlayer = teamPlayerRepository.findByTeamIdAndPlayerId(teamId, playerId)
                .orElseThrow(() -> new TournamentServiceException("Player not found", HttpStatus.NOT_FOUND));

        if (teamPlayer.getTeamPlayerRole().equals(TeamPlayerRole.CAPTAIN) ||
            teamPlayer.getTeamPlayerRole().equals(TeamPlayerRole.VICE_CAPTAIN)) {
            teamPlayer.setTeamPlayerRole(TeamPlayerRole.PLAYER);
            teamPlayerRepository.save(teamPlayer);
        }
    }

    @Override
    public List<TeamPlayerResponse> getCaptainsByTeamId(Long teamId) {
        List<TeamPlayer> captains = teamPlayerRepository.findCaptainsByTeamId(teamId);
        return captains.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public boolean isCaptainOfTeam(Long teamId, Long playerId) {
        return teamPlayerRepository.isCaptainOfTeam(teamId, playerId);
    }

    @Override
    public boolean isCaptainOrViceCaptain(Long teamId, Long playerId) {
        return teamPlayerRepository.findCaptainByTeamIdAndPlayerId(teamId, playerId).isPresent();
    }

    @Override
    public void validateCaptainOwnership(Long teamId, Long playerId) {
        boolean isCaptain = isCaptainOfTeam(teamId, playerId);
        if (!isCaptain) {
            throw new TournamentServiceException("Only team captains can edit team fixtures", HttpStatus.FORBIDDEN);
        }
    }

    private TeamPlayerResponse convertToResponse(TeamPlayer teamPlayer) {
        return TeamPlayerResponse.builder()
                .id(teamPlayer.getId())
                .teamId(teamPlayer.getTeam().getId())
                .teamName(teamPlayer.getTeam().getTeamName())
                .playerId(teamPlayer.getPlayer().getId())
                .playerName(teamPlayer.getPlayer().getName())
                .playingPosition(teamPlayer.getPlayingPosition())
                .teamPlayerRole(teamPlayer.getTeamPlayerRole() != null ? teamPlayer.getTeamPlayerRole().toString() : null)
                .isCaptain(teamPlayer.getIsCaptain())
                .jerseyNumber(teamPlayer.getJerseyNumber())
                .build();
    }

}
