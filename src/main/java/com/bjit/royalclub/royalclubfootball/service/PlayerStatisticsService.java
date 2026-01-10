package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PlayerStatisticsFilterRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerStatisticsResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentTopScorerResponse;

import java.util.List;

public interface PlayerStatisticsService {

    /**
     * Get player statistics with optional filters
     *
     * @param filterRequest Filter parameters (tournament, position, sorting, etc.)
     * @return List of PlayerStatisticsResponse with filtered and sorted player statistics
     */
    List<PlayerStatisticsResponse> getPlayerStatistics(PlayerStatisticsFilterRequest filterRequest);

    /**
     * Get top scorers for a specific tournament
     *
     * @param tournamentId The tournament ID
     * @param limit        Maximum number of top scorers to return
     * @return List of TournamentTopScorerResponse with top scorers
     */
    List<TournamentTopScorerResponse> getTopScorersByTournament(Long tournamentId, Integer limit);
}
