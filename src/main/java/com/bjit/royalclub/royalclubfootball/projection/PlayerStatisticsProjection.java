package com.bjit.royalclub.royalclubfootball.projection;

/**
 * Projection interface for aggregated player statistics
 */
public interface PlayerStatisticsProjection {
    Long getPlayerId();
    Long getGoalsScored();
    Long getAssists();
    Long getMatchesPlayed();
    Long getYellowCards();
    Long getRedCards();
}
