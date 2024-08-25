package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import jakarta.transaction.Transactional;

public interface TeamManagementService {
    @Transactional
    TeamResponse createOrUpdateTeam(TeamRequest teamRequest);

    void deleteTeam(Long teamId);
}
