package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TeamCaptainService {

    /**
     * Set a player as captain of a team
     */
    @Transactional
    TeamPlayerResponse setCaptain(Long teamId, Long playerId);

    /**
     * Set a player as vice-captain of a team
     */
    @Transactional
    TeamPlayerResponse setViceCaptain(Long teamId, Long playerId);

    /**
     * Remove captain role from a player
     */
    @Transactional
    void removeCaptain(Long teamId, Long playerId);

    /**
     * Get all captains for a team
     */
    List<TeamPlayerResponse> getCaptainsByTeamId(Long teamId);

    /**
     * Check if a player is captain of a team
     */
    boolean isCaptainOfTeam(Long teamId, Long playerId);

    /**
     * Check if a player is captain or vice-captain
     */
    boolean isCaptainOrViceCaptain(Long teamId, Long playerId);

    /**
     * Validate that player owns the fixture permission (is captain of their team)
     */
    void validateCaptainOwnership(Long teamId, Long playerId);

}
