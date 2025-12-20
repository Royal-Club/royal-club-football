package com.bjit.royalclub.royalclubfootball.projection;

/**
 * Projection interface for team information
 */
public interface TeamInfoProjection {
    Long getTeamId();
    String getTeamName();
    Long getTournamentId();
    String getTournamentName();
}

