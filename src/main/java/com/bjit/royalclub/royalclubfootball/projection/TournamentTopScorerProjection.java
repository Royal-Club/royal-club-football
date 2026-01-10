package com.bjit.royalclub.royalclubfootball.projection;

/**
 * Projection interface for tournament top scorer statistics
 */
public interface TournamentTopScorerProjection {
    Long getPlayerId();
    String getPlayerName();
    Long getTeamId();
    String getTeamName();
    String getPosition();
    Long getGoalsScored();
    Long getAssists();
    Long getMatchesPlayed();
}
