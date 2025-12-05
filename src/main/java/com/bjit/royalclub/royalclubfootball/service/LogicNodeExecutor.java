package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.*;
import com.bjit.royalclub.royalclubfootball.enums.RoundType;
import com.bjit.royalclub.royalclubfootball.enums.TeamAssignmentType;
import com.bjit.royalclub.royalclubfootball.exception.RoundServiceException;
import com.bjit.royalclub.royalclubfootball.model.AdvancedTeamsResponse;
import com.bjit.royalclub.royalclubfootball.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogicNodeExecutor {

    private final GroupStandingRepository groupStandingRepository;
    private final RoundGroupTeamRepository roundGroupTeamRepository;
    private final RoundTeamRepository roundTeamRepository;
    private final TeamRepository teamRepository;
    private final RoundGroupRepository roundGroupRepository;
    private final ObjectMapper objectMapper;

    /**
     * Execute a logic node - apply rules and advance teams
     */
    @Transactional
    public AdvancedTeamsResponse executeLogicNode(LogicNode logicNode) {
        log.info("Executing logic node ID: {} - {}", logicNode.getId(), logicNode.getNodeName());

        try {
            // Parse rule config
            JsonNode ruleConfig = objectMapper.readTree(logicNode.getRuleConfig());
            String ruleType = ruleConfig.has("type") ? ruleConfig.get("type").asText() : "TOP_N_FROM_EACH";
            int topN = ruleConfig.has("topN") ? ruleConfig.get("topN").asInt() : 2;

            // Get teams to advance based on source type
            List<Team> teamsToAdvance = new ArrayList<>();

            if (logicNode.getSourceRound() != null) {
                // Source is a round
                teamsToAdvance = getTeamsFromRound(logicNode.getSourceRound(), ruleType, topN, ruleConfig);
            } else if (logicNode.getSourceGroup() != null) {
                // Source is a group
                teamsToAdvance = getTeamsFromGroup(logicNode.getSourceGroup(), ruleType, topN, ruleConfig);
            } else {
                throw new RoundServiceException(
                        "Logic node must have either sourceRound or sourceGroup",
                        HttpStatus.BAD_REQUEST);
            }

            if (teamsToAdvance.isEmpty()) {
                log.warn("No teams found to advance for logic node ID: {}", logicNode.getId());
                return null;
            }

            // Assign teams to target round
            List<AdvancedTeamsResponse.TeamAdvancementInfo> advancedTeams = assignTeamsToTargetRound(
                    teamsToAdvance, logicNode.getTargetRound(), logicNode);

            log.info("Logic node executed: {} teams advanced to round {}", 
                    advancedTeams.size(), logicNode.getTargetRound().getRoundName());

            return AdvancedTeamsResponse.builder()
                    .sourceRoundId(logicNode.getSourceRound() != null ? logicNode.getSourceRound().getId() : null)
                    .sourceRoundName(logicNode.getSourceRound() != null ? logicNode.getSourceRound().getRoundName() : null)
                    .targetRoundId(logicNode.getTargetRound().getId())
                    .targetRoundName(logicNode.getTargetRound().getRoundName())
                    .teamsAdvanced(advancedTeams.size())
                    .teams(advancedTeams)
                    .build();

        } catch (Exception e) {
            log.error("Failed to execute logic node ID: {}", logicNode.getId(), e);
            throw new RoundServiceException(
                    "Failed to execute logic node: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get teams from a round based on rule type
     */
    private List<Team> getTeamsFromRound(TournamentRound sourceRound, String ruleType, int topN, JsonNode ruleConfig) {
        List<Team> teams = new ArrayList<>();

        if ("TOP_N_FROM_EACH".equals(ruleType)) {
            // Get top N from each group in the round
            List<RoundGroup> groups = roundGroupRepository.findByRoundId(sourceRound.getId());
            for (RoundGroup group : groups) {
                List<Team> topTeams = getTopNTeamsFromGroup(group, topN, ruleConfig);
                teams.addAll(topTeams);
            }
        } else if ("TOP_N_OVERALL".equals(ruleType)) {
            // Get top N teams across all groups in the round
            List<RoundGroup> groups = roundGroupRepository.findByRoundId(sourceRound.getId());
            List<GroupStanding> allStandings = new ArrayList<>();
            for (RoundGroup group : groups) {
                allStandings.addAll(groupStandingRepository.findByGroupId(group.getId()));
            }
            // Sort all standings and get top N
            List<GroupStanding> sortedStandings = sortStandings(allStandings, ruleConfig);
            teams = sortedStandings.stream()
                    .limit(topN)
                    .map(GroupStanding::getTeam)
                    .collect(Collectors.toList());
        } else if ("ALL_TEAMS".equals(ruleType)) {
            // Get all teams from the round
            if (sourceRound.getRoundType() == RoundType.GROUP_BASED) {
                List<RoundGroup> groups = roundGroupRepository.findByRoundId(sourceRound.getId());
                for (RoundGroup group : groups) {
                    List<RoundGroupTeam> groupTeams = roundGroupTeamRepository.findByGroupId(group.getId());
                    teams.addAll(groupTeams.stream()
                            .filter(rgt -> !rgt.getIsPlaceholder() && rgt.getTeam() != null)
                            .map(RoundGroupTeam::getTeam)
                            .collect(Collectors.toList()));
                }
            } else {
                // Direct knockout
                List<RoundTeam> roundTeams = roundTeamRepository.findByRoundId(sourceRound.getId());
                teams = roundTeams.stream()
                        .filter(rt -> !rt.getIsPlaceholder() && rt.getTeam() != null)
                        .map(RoundTeam::getTeam)
                        .collect(Collectors.toList());
            }
        }

        return teams;
    }

    /**
     * Get teams from a group based on rule type
     */
    private List<Team> getTeamsFromGroup(RoundGroup sourceGroup, String ruleType, int topN, JsonNode ruleConfig) {
        if ("TOP_N_FROM_EACH".equals(ruleType) || "TOP_N_OVERALL".equals(ruleType)) {
            return getTopNTeamsFromGroup(sourceGroup, topN, ruleConfig);
        } else if ("ALL_TEAMS".equals(ruleType)) {
            List<RoundGroupTeam> groupTeams = roundGroupTeamRepository.findByGroupId(sourceGroup.getId());
            return groupTeams.stream()
                    .filter(rgt -> !rgt.getIsPlaceholder() && rgt.getTeam() != null)
                    .map(RoundGroupTeam::getTeam)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Get top N teams from a group with tie-breaker rules
     */
    private List<Team> getTopNTeamsFromGroup(RoundGroup group, int topN, JsonNode ruleConfig) {
            List<GroupStanding> standings = groupStandingRepository.findByGroupId(group.getId());

        if (standings.isEmpty()) {
            log.warn("No standings found for group ID: {}", group.getId());
            return new ArrayList<>();
        }

        // Sort standings using tie-breaker rules
        List<GroupStanding> sortedStandings = sortStandings(standings, ruleConfig);

        // Get top N teams
        return sortedStandings.stream()
                .limit(topN)
                .map(GroupStanding::getTeam)
                .collect(Collectors.toList());
    }

    /**
     * Sort standings based on tie-breaker rules
     */
    private List<GroupStanding> sortStandings(List<GroupStanding> standings, JsonNode ruleConfig) {
        // Get tie-breaker configuration
        String primary = ruleConfig.has("tieBreakerRules") && ruleConfig.get("tieBreakerRules").has("primary")
                ? ruleConfig.get("tieBreakerRules").get("primary").asText()
                : "POINTS";
        String secondary = ruleConfig.has("tieBreakerRules") && ruleConfig.get("tieBreakerRules").has("secondary")
                ? ruleConfig.get("tieBreakerRules").get("secondary").asText()
                : "GOAL_DIFFERENCE";
        String tertiary = ruleConfig.has("tieBreakerRules") && ruleConfig.get("tieBreakerRules").has("tertiary")
                ? ruleConfig.get("tieBreakerRules").get("tertiary").asText()
                : "HEAD_TO_HEAD";

        // Create a mutable copy for sorting
        List<GroupStanding> sorted = new ArrayList<>(standings);

        sorted.sort((a, b) -> {
            // Primary: Points
            if ("POINTS".equals(primary)) {
                int pointsCompare = Integer.compare(
                        b.getPoints() != null ? b.getPoints() : 0,
                        a.getPoints() != null ? a.getPoints() : 0);
                if (pointsCompare != 0) return pointsCompare;
            }

            // Secondary: Goal Difference
            if ("GOAL_DIFFERENCE".equals(secondary)) {
                int gdCompare = Integer.compare(
                        b.getGoalDifference() != null ? b.getGoalDifference() : 0,
                        a.getGoalDifference() != null ? a.getGoalDifference() : 0);
                if (gdCompare != 0) return gdCompare;
            }

            // Tertiary: Goals For
            if ("GOALS_FOR".equals(tertiary)) {
                int gfCompare = Integer.compare(
                        b.getGoalsFor() != null ? b.getGoalsFor() : 0,
                        a.getGoalsFor() != null ? a.getGoalsFor() : 0);
                if (gfCompare != 0) return gfCompare;
            }

            // If still tied, use position if available
            if (a.getPosition() != null && b.getPosition() != null) {
                return Integer.compare(a.getPosition(), b.getPosition());
            }

            return 0;
        });

        return sorted;
    }

    /**
     * Assign teams to target round
     */
    private List<AdvancedTeamsResponse.TeamAdvancementInfo> assignTeamsToTargetRound(
            List<Team> teams, TournamentRound targetRound, LogicNode logicNode) {

        List<AdvancedTeamsResponse.TeamAdvancementInfo> advancedTeams = new ArrayList<>();

        for (Team team : teams) {
            // Check if team already assigned to target round
            if (targetRound.getRoundType() == RoundType.GROUP_BASED) {
                // For group-based rounds, assign to first group if available
                List<RoundGroup> groups = roundGroupRepository.findByRoundId(targetRound.getId());
                if (!groups.isEmpty()) {
                    RoundGroup firstGroup = groups.get(0);
                    if (!roundGroupTeamRepository.existsByGroupIdAndTeamId(firstGroup.getId(), team.getId())) {
                        RoundGroupTeam roundGroupTeam = RoundGroupTeam.builder()
                                .group(firstGroup)
                                .team(team)
                                .assignmentType(TeamAssignmentType.MANUAL)
                                .isPlaceholder(false)
                                .build();
                        roundGroupTeamRepository.save(roundGroupTeam);
                    }
                } else {
                    // No groups yet, create a round team entry
                    if (!roundTeamRepository.existsByRoundIdAndTeamId(targetRound.getId(), team.getId())) {
                        RoundTeam roundTeam = RoundTeam.builder()
                                .round(targetRound)
                                .team(team)
                                .assignmentType(TeamAssignmentType.MANUAL)
                                .isPlaceholder(false)
                                .build();
                        roundTeamRepository.save(roundTeam);
                    }
                }
            } else {
                // Direct knockout round
                if (!roundTeamRepository.existsByRoundIdAndTeamId(targetRound.getId(), team.getId())) {
                    RoundTeam roundTeam = RoundTeam.builder()
                            .round(targetRound)
                            .team(team)
                            .assignmentType(TeamAssignmentType.MANUAL)
                            .isPlaceholder(false)
                            .build();
                    roundTeamRepository.save(roundTeam);
                }
            }

            advancedTeams.add(AdvancedTeamsResponse.TeamAdvancementInfo.builder()
                    .teamId(team.getId())
                    .teamName(team.getTeamName())
                    .advancementReason("LOGIC_NODE: " + logicNode.getNodeName())
                    .build());
        }

        return advancedTeams;
    }
}

