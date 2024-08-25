package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRemoveRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import jakarta.transaction.Transactional;

public interface TeamManagementService {
    @Transactional
    TeamResponse createOrUpdateTeam(TeamRequest teamRequest);

    @Transactional
    void deleteTeam(Long teamId);

    @Transactional
    TeamPlayerResponse saveOrUpdateTeamPlayer(TeamPlayerRequest teamPlayerRequest);

    void removePlayerFromTeam(TeamPlayerRemoveRequest playerRemoveRequest);
}
