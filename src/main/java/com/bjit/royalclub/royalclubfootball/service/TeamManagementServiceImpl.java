package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TeamManagementServiceImpl implements TeamManagementService {

    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;

    @Override
    public TeamResponse createOrUpdateTeam(TeamRequest teamRequest) {
        Tournament tournament = tournamentRepository.findById(teamRequest.getTournamentId())
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        if (tournament.getTournamentDate().isBefore(LocalDateTime.now())) {
            throw new TournamentServiceException(TOURNAMENT_DATE_CAT_NOT_BE_PAST_DATE, HttpStatus.BAD_REQUEST);
        }
        Team team;
        if (teamRequest.getId() == null) {
            team = Team.builder()
                    .teamName(teamRequest.getTeamName())
                    .tournament(tournament)
                    .createdDate(LocalDateTime.now())
                    .build();
        } else {
            team = teamRepository.findById(teamRequest.getId())
                    .orElseThrow(() -> new TournamentServiceException(TEAM_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
            team.setTeamName(teamRequest.getTeamName());
            team.setUpdatedDate(LocalDateTime.now());
        }
        teamRepository.save(team);
        return convertToDto(team);
    }

    public TeamResponse convertToDto(Team team) {
        return TeamResponse.builder()
                .teamId(team.getId())
                .tournamentName(team.getTeamName())
                .tournamentId(team.getTournament().getId())
                .build();
    }

}
