package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.model.MatchEventRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchEventResponse;
import com.bjit.royalclub.royalclubfootball.model.MatchResponse;
import com.bjit.royalclub.royalclubfootball.model.MatchUpdateRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface MatchManagementService {

    /**
     * Get match details by ID
     */
    MatchResponse getMatchById(Long matchId);

    /**
     * Start a match (change status to ONGOING)
     */
    @Transactional
    MatchResponse startMatch(Long matchId);

    /**
     * Pause a match (keep status as ONGOING but stop the clock)
     */
    @Transactional
    MatchResponse pauseMatch(Long matchId);

    /**
     * Resume a paused match
     */
    @Transactional
    MatchResponse resumeMatch(Long matchId);

    /**
     * Complete a match (change status to COMPLETED)
     */
    @Transactional
    MatchResponse completeMatch(Long matchId);

    /**
     * Update match scores and time
     */
    @Transactional
    MatchResponse updateMatch(Long matchId, MatchUpdateRequest updateRequest);

    /**
     * Record a match event (goal, card, substitution, etc.)
     */
    @Transactional
    MatchEventResponse recordMatchEvent(MatchEventRequest eventRequest);

    /**
     * Get all events for a specific match
     */
    List<MatchEventResponse> getMatchEvents(Long matchId);

    /**
     * Update elapsed time without changing other match data
     */
    @Transactional
    void updateElapsedTime(Long matchId, Integer elapsedSeconds);

    /**
     * Update team score
     */
    @Transactional
    void updateTeamScore(Long matchId, Long teamId, Integer newScore);

    /**
     * Delete a match event and reverse score if it was a goal
     */
    @Transactional
    void deleteMatchEvent(Long eventId);

}
