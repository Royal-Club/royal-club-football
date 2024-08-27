package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRemoveRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TeamManagementService {
    @Transactional
    TeamResponse createOrUpdateTeam(TeamRequest teamRequest);

    @Transactional
    void deleteTeam(Long teamId);

    @Transactional
    TeamPlayerResponse saveOrUpdateTeamPlayer(TeamPlayerRequest teamPlayerRequest);

    @Transactional
    void removePlayerFromTeam(TeamPlayerRemoveRequest playerRemoveRequest);

    List<TournamentResponse> getTournamentsSummery(Long tournamentId);
}
