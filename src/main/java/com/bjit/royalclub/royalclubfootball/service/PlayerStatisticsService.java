package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PlayerStatisticsFilterRequest;
import com.bjit.royalclub.royalclubfootball.model.PlayerStatisticsResponse;

import java.util.List;

public interface PlayerStatisticsService {

    /**
     * Get player statistics with optional filters
     *
     * @param filterRequest Filter parameters (tournament, position, sorting, etc.)
     * @return List of PlayerStatisticsResponse with filtered and sorted player statistics
     */
    List<PlayerStatisticsResponse> getPlayerStatistics(PlayerStatisticsFilterRequest filterRequest);
}
