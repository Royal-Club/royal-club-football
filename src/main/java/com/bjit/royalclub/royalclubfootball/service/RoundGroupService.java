package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.RoundGroupRequest;
import com.bjit.royalclub.royalclubfootball.model.RoundGroupResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamAssignmentRequest;
import com.bjit.royalclub.royalclubfootball.model.PlaceholderTeamRequest;
import com.bjit.royalclub.royalclubfootball.model.GroupStandingResponse;
import com.bjit.royalclub.royalclubfootball.model.GroupTiebreakRequest;
import com.bjit.royalclub.royalclubfootball.model.GroupMatchGenerationRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface RoundGroupService {

    @Transactional
    RoundGroupResponse createGroup(RoundGroupRequest request);

    @Transactional
    RoundGroupResponse updateGroup(Long groupId, RoundGroupRequest request);

    @Transactional
    void deleteGroup(Long groupId);

    RoundGroupResponse getGroupById(Long groupId);

    List<RoundGroupResponse> getGroupsByRoundId(Long roundId);

    @Transactional
    void assignTeamsToGroup(Long groupId, TeamAssignmentRequest request);

    @Transactional
    void createPlaceholderTeam(PlaceholderTeamRequest request);

    @Transactional
    void removeTeamFromGroup(Long groupId, Long teamId);

    List<GroupStandingResponse> getGroupStandings(Long groupId);

    @Transactional
    void recalculateGroupStandings(Long groupId);

    /**
     * Record a manual penalty-shootout tiebreak for teams that are level on
     * every other criterion, then re-rank the group.
     */
    @Transactional
    List<GroupStandingResponse> applyGroupTiebreak(Long groupId, GroupTiebreakRequest request);

    /**
     * Generate round-robin matches for all teams in a group
     * @param groupId Group ID
     * @param request Match generation configuration
     * @return List of generated matches as MatchResponse DTOs
     */
    @Transactional
    List<MatchResponse> generateGroupMatches(Long groupId, GroupMatchGenerationRequest request);

    /**
     * Get all matches for a specific group
     * @param groupId Group ID
     * @return List of matches as MatchResponse DTOs
     */
    List<MatchResponse> getGroupMatches(Long groupId);

    /**
     * Clear all matches for a specific group
     * @param groupId Group ID
     */
    @Transactional
    void clearGroupMatches(Long groupId);
}
