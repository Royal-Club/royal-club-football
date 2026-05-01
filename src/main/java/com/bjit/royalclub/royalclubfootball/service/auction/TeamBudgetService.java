package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.model.auction.TeamBudgetRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.TeamBudgetResponse;

import java.util.List;

public interface TeamBudgetService {
    List<TeamBudgetResponse> getTeamBudgets(Long tournamentId);
    TeamBudgetResponse createTeamBudget(Long tournamentId, TeamBudgetRequest request);
    TeamBudgetResponse updateTeamBudget(Long tournamentId, Long id, TeamBudgetRequest request);
    void deleteTeamBudget(Long tournamentId, Long id);
}
