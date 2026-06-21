package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.*;
import com.bjit.royalclub.royalclubfootball.enums.FixtureFormat;
import com.bjit.royalclub.royalclubfootball.enums.GroupFormat;
import com.bjit.royalclub.royalclubfootball.enums.MatchEventType;
import com.bjit.royalclub.royalclubfootball.enums.MatchStatus;
import com.bjit.royalclub.royalclubfootball.enums.RoundStatus;
import com.bjit.royalclub.royalclubfootball.enums.TeamAssignmentType;
import com.bjit.royalclub.royalclubfootball.exception.RoundServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TeamServiceException;
import com.bjit.royalclub.royalclubfootball.model.*;
import com.bjit.royalclub.royalclubfootball.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoundGroupServiceImpl implements RoundGroupService {

    private final RoundGroupRepository roundGroupRepository;
    private final TournamentRoundRepository tournamentRoundRepository;
    private final RoundGroupTeamRepository roundGroupTeamRepository;
    private final GroupStandingRepository groupStandingRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final MatchEventRepository matchEventRepository;
    private final VenueRepository venueRepository;

    @Override
    public RoundGroupResponse createGroup(RoundGroupRequest request) {
        log.info("Creating group for round ID: {}", request.getRoundId());

        TournamentRound round = tournamentRoundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Validate group name uniqueness within the round
        if (roundGroupRepository.existsByRoundIdAndGroupName(request.getRoundId(), request.getGroupName())) {
            throw new RoundServiceException(GROUP_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        RoundGroup parentGroup = null;
        if (request.getParentGroupId() != null) {
            parentGroup = roundGroupRepository.findById(request.getParentGroupId())
                    .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

            if (!parentGroup.getRound().getId().equals(request.getRoundId())) {
                throw new RoundServiceException(PARENT_GROUP_NOT_IN_SAME_ROUND, HttpStatus.BAD_REQUEST);
            }
        }

        RoundGroup group = RoundGroup.builder()
                .round(round)
                .parentGroup(parentGroup)
                .groupName(request.getGroupName())
                .groupFormat(request.getGroupFormat() != null ?
                        GroupFormat.valueOf(request.getGroupFormat()) : GroupFormat.MANUAL)
                .advancementRule(request.getAdvancementRule())
                .maxTeams(request.getMaxTeams())
                .status(RoundStatus.NOT_STARTED)
                .build();

        RoundGroup savedGroup = roundGroupRepository.save(group);
        log.info("Group created successfully with ID: {}", savedGroup.getId());

        return convertToGroupResponse(savedGroup);
    }

    @Override
    public RoundGroupResponse updateGroup(Long groupId, RoundGroupRequest request) {
        log.info("Updating group ID: {}", groupId);

        RoundGroup group = roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check if group name is being changed and if new name already exists
        if (!group.getGroupName().equals(request.getGroupName()) &&
                roundGroupRepository.existsByRoundIdAndGroupName(request.getRoundId(), request.getGroupName())) {
            throw new RoundServiceException(GROUP_NAME_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        group.setGroupName(request.getGroupName());
        if (request.getGroupFormat() != null) {
            group.setGroupFormat(GroupFormat.valueOf(request.getGroupFormat()));
        }
        group.setAdvancementRule(request.getAdvancementRule());
        group.setMaxTeams(request.getMaxTeams());

        RoundGroup updatedGroup = roundGroupRepository.save(group);
        log.info("Group updated successfully with ID: {}", updatedGroup.getId());

        return convertToGroupResponse(updatedGroup);
    }

    @Override
    public void deleteGroup(Long groupId) {
        log.info("Deleting group ID: {}", groupId);

        RoundGroup group = roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check if group has sub-groups
        if (!group.getChildGroups().isEmpty()) {
            throw new RoundServiceException(GROUP_HAS_SUB_GROUPS, HttpStatus.CONFLICT);
        }

        // Check if group has teams
        long teamCount = roundGroupTeamRepository.countByGroupId(groupId);
        if (teamCount > 0) {
            throw new RoundServiceException(GROUP_HAS_TEAMS, HttpStatus.CONFLICT);
        }

        roundGroupRepository.delete(group);
        log.info("Group deleted successfully with ID: {}", groupId);
    }

    @Override
    public RoundGroupResponse getGroupById(Long groupId) {
        log.info("Fetching group by ID: {}", groupId);

        RoundGroup group = roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        return convertToGroupResponse(group);
    }

    @Override
    public List<RoundGroupResponse> getGroupsByRoundId(Long roundId) {
        log.info("Fetching all groups for round ID: {}", roundId);

        List<RoundGroup> groups = roundGroupRepository.findTopLevelByRoundId(roundId);

        return groups.stream()
                .map(this::convertToGroupResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void assignTeamsToGroup(Long groupId, TeamAssignmentRequest request) {
        log.info("Assigning {} teams to group ID: {}", request.getTeamIds().size(), groupId);

        RoundGroup group = roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check if adding teams would exceed max capacity
        long currentTeamCount = roundGroupTeamRepository.countByGroupId(groupId);
        if (group.getMaxTeams() != null &&
                (currentTeamCount + request.getTeamIds().size()) > group.getMaxTeams()) {
            throw new RoundServiceException(GROUP_MAX_TEAMS_REACHED, HttpStatus.BAD_REQUEST);
        }

        List<RoundGroupTeam> teamAssignments = new ArrayList<>();

        for (Long teamId : request.getTeamIds()) {
            // Check if team already in group
            if (roundGroupTeamRepository.existsByGroupIdAndTeamId(groupId, teamId)) {
                throw new RoundServiceException(TEAM_ALREADY_IN_GROUP, HttpStatus.CONFLICT);
            }

            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new TeamServiceException(TEAM_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

            RoundGroupTeam roundGroupTeam = RoundGroupTeam.builder()
                    .group(group)
                    .team(team)
                    .assignmentType(TeamAssignmentType.MANUAL)
                    .isPlaceholder(false)
                    .build();

            teamAssignments.add(roundGroupTeam);

            // Create standing entry for the team
            if (!groupStandingRepository.existsByGroupIdAndTeamId(groupId, teamId)) {
                GroupStanding standing = GroupStanding.builder()
                        .group(group)
                        .team(team)
                        .matchesPlayed(0)
                        .wins(0)
                        .draws(0)
                        .losses(0)
                        .goalsFor(0)
                        .goalsAgainst(0)
                        .goalDifference(0)
                        .points(0)
                        .isAdvanced(false)
                        .build();
                groupStandingRepository.save(standing);
            }
        }

        roundGroupTeamRepository.saveAll(teamAssignments);
        log.info("Successfully assigned {} teams to group ID: {}", teamAssignments.size(), groupId);
    }

    @Override
    public void createPlaceholderTeam(PlaceholderTeamRequest request) {
        log.info("Creating placeholder team: {}", request.getPlaceholderName());

        if (request.getGroupId() != null) {
            // Placeholder for group-based round
            RoundGroup group = roundGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

            RoundGroupTeam placeholder = RoundGroupTeam.builder()
                    .group(group)
                    .team(null)
                    .assignmentType(TeamAssignmentType.PLACEHOLDER)
                    .isPlaceholder(true)
                    .placeholderName(request.getPlaceholderName())
                    .build();

            roundGroupTeamRepository.save(placeholder);
            log.info("Placeholder team created in group ID: {}", request.getGroupId());
        } else if (request.getRoundId() != null) {
            // Placeholder for direct knockout round
            log.warn("Direct knockout round placeholder creation not implemented in this service");
            throw new RoundServiceException("Use TeamAssignmentService for direct round placeholders",
                    HttpStatus.BAD_REQUEST);
        } else {
            throw new RoundServiceException("Either groupId or roundId must be provided",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void removeTeamFromGroup(Long groupId, Long teamId) {
        log.info("Removing team ID: {} from group ID: {}", teamId, groupId);

        RoundGroup group = roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check if team is in group
        if (!roundGroupTeamRepository.existsByGroupIdAndTeamId(groupId, teamId)) {
            throw new RoundServiceException("Team is not in this group", HttpStatus.NOT_FOUND);
        }

        // Check if team has matches in this group
        long matchCount = matchRepository.countByGroupIdAndTeamId(groupId, teamId);
        if (matchCount > 0) {
            throw new RoundServiceException("Cannot remove team with existing matches",
                    HttpStatus.CONFLICT);
        }

        roundGroupTeamRepository.deleteByGroupIdAndTeamId(groupId, teamId);
        groupStandingRepository.deleteByGroupIdAndTeamId(groupId, teamId);

        log.info("Team removed successfully from group ID: {}", groupId);
    }

    @Override
    public List<GroupStandingResponse> getGroupStandings(Long groupId) {
        log.info("Fetching standings for group ID: {}", groupId);

        RoundGroup group = roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<GroupStanding> standings = rankStandings(groupId, groupStandingRepository.findByGroupId(groupId));

        return standings.stream()
                .map(this::convertToStandingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void recalculateGroupStandings(Long groupId) {
        log.info("Recalculating standings for group ID: {}", groupId);

        RoundGroup group = roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<GroupStanding> standings = groupStandingRepository.findByGroupId(groupId);

        // Reset all standings (tiebreakRank is preserved: it is a manual override)
        for (GroupStanding standing : standings) {
            standing.setMatchesPlayed(0);
            standing.setWins(0);
            standing.setDraws(0);
            standing.setLosses(0);
            standing.setGoalsFor(0);
            standing.setGoalsAgainst(0);
            standing.setGoalDifference(0);
            standing.setPoints(0);
            standing.setYellowCards(0);
            standing.setRedCards(0);
            standing.setFairPlayPoints(0);
        }

        // Recalculate from completed matches
        List<Match> matches = matchRepository.findCompletedByGroupId(groupId);

        for (Match match : matches) {
            updateStandingsForMatch(standings, match);
            aggregateCardsForMatch(standings, match);
        }

        // Rank using the full criteria sequence and assign positions
        rankStandings(groupId, standings);

        // Save updated standings
        groupStandingRepository.saveAll(standings);

        log.info("Group standings recalculated successfully for group ID: {}", groupId);
    }

    @Override
    public List<GroupStandingResponse> applyGroupTiebreak(Long groupId, GroupTiebreakRequest request) {
        log.info("Applying manual tiebreak for group ID: {}", groupId);

        roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<GroupStanding> standings = groupStandingRepository.findByGroupId(groupId);
        Map<Long, GroupStanding> standingByTeamId = standings.stream()
                .collect(Collectors.toMap(s -> s.getTeam().getId(), s -> s));

        List<Long> orderedTeamIds = request.getOrderedTeamIds();
        for (int i = 0; i < orderedTeamIds.size(); i++) {
            Long teamId = orderedTeamIds.get(i);
            GroupStanding standing = standingByTeamId.get(teamId);
            if (standing == null) {
                throw new RoundServiceException("Team " + teamId + " is not in this group", HttpStatus.BAD_REQUEST);
            }
            standing.setTiebreakRank(i + 1);
        }

        rankStandings(groupId, standings);
        groupStandingRepository.saveAll(standings);

        log.info("Manual tiebreak applied and group {} re-ranked", groupId);
        return standings.stream()
                .sorted(Comparator.comparing(GroupStanding::getPosition,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::convertToStandingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Aggregate yellow/red cards and UEFA fair-play deductions for one match
     * into the affected team standings.
     */
    private void aggregateCardsForMatch(List<GroupStanding> standings, Match match) {
        List<MatchEvent> events = matchEventRepository.findByMatchId(match.getId());
        if (events.isEmpty()) {
            return;
        }

        // teamId -> (playerId -> [yellowCount, redCount]); null player bucketed under -1
        Map<Long, Map<Long, int[]>> cardsByTeamPlayer = new HashMap<>();
        for (MatchEvent event : events) {
            MatchEventType type = event.getEventType();
            if (type != MatchEventType.YELLOW_CARD && type != MatchEventType.RED_CARD) {
                continue;
            }
            if (event.getTeam() == null) {
                continue;
            }
            Long teamId = event.getTeam().getId();
            Long playerId = event.getPlayer() != null ? event.getPlayer().getId() : -1L;
            int[] tally = cardsByTeamPlayer
                    .computeIfAbsent(teamId, k -> new HashMap<>())
                    .computeIfAbsent(playerId, k -> new int[2]);
            if (type == MatchEventType.YELLOW_CARD) {
                tally[0]++;
            } else {
                tally[1]++;
            }
        }

        cardsByTeamPlayer.forEach((teamId, playerTallies) -> {
            GroupStanding standing = standings.stream()
                    .filter(s -> s.getTeam().getId().equals(teamId))
                    .findFirst()
                    .orElse(null);
            if (standing == null) {
                return;
            }
            int yellow = 0;
            int red = 0;
            int fairPlayDeduction = 0;
            for (int[] tally : playerTallies.values()) {
                yellow += tally[0];
                red += tally[1];
                fairPlayDeduction += fairPlayDeduction(tally[0], tally[1]);
            }
            standing.setYellowCards(standing.getYellowCards() + yellow);
            standing.setRedCards(standing.getRedCards() + red);
            standing.setFairPlayPoints(standing.getFairPlayPoints() + fairPlayDeduction);
        });
    }

    /**
     * UEFA fair-play deduction for a single player in a single match
     * (lower total is worse). Standard cases:
     * single yellow = -1, second yellow (indirect red) = -3,
     * direct red = -4, yellow then direct red = -5.
     */
    private int fairPlayDeduction(int yellowCount, int redCount) {
        if (yellowCount >= 2) {
            return -3;
        }
        if (yellowCount == 1 && redCount >= 1) {
            return -5;
        }
        if (redCount >= 1) {
            return -4;
        }
        if (yellowCount == 1) {
            return -1;
        }
        return 0;
    }

    /**
     * Rank standings using the full group ordering sequence and assign 1-based
     * positions in place:
     * Points -> Goal Difference -> Goals For -> Head-to-Head -> Fair Play
     * -> Penalty Shootout (manual tiebreak) -> Team name.
     * Returns the standings sorted in ranking order.
     */
    private List<GroupStanding> rankStandings(Long groupId, List<GroupStanding> standings) {
        List<Match> completedMatches = matchRepository.findCompletedByGroupId(groupId);
        List<GroupStanding> ranked = rankCluster(new ArrayList<>(standings), completedMatches, 0);
        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setPosition(i + 1);
        }
        return ranked;
    }

    // Ordering stages applied in sequence; ties at one stage are broken by the next.
    private static final int STAGE_POINTS = 0;
    private static final int STAGE_GOAL_DIFFERENCE = 1;
    private static final int STAGE_GOALS_FOR = 2;
    private static final int STAGE_HEAD_TO_HEAD = 3;
    private static final int STAGE_FAIR_PLAY = 4;
    private static final int STAGE_TIEBREAK = 5;
    private static final int STAGE_TEAM_NAME = 6;

    /**
     * Recursively rank a cluster of teams that are tied up to {@code stage}.
     * At each stage the cluster is sorted, then sub-clusters that remain tied
     * are resolved by the next stage. Head-to-head is computed relative to the
     * current (tied) cluster only, which keeps multi-team ties correct.
     */
    private List<GroupStanding> rankCluster(List<GroupStanding> cluster, List<Match> matches, int stage) {
        if (cluster.size() <= 1 || stage > STAGE_TEAM_NAME) {
            return cluster;
        }

        Comparator<GroupStanding> comparator = comparatorForStage(stage, cluster, matches);
        cluster.sort(comparator);

        List<GroupStanding> result = new ArrayList<>(cluster.size());
        int i = 0;
        while (i < cluster.size()) {
            int j = i + 1;
            while (j < cluster.size() && comparator.compare(cluster.get(i), cluster.get(j)) == 0) {
                j++;
            }
            List<GroupStanding> tied = new ArrayList<>(cluster.subList(i, j));
            if (tied.size() > 1) {
                result.addAll(rankCluster(tied, matches, stage + 1));
            } else {
                result.addAll(tied);
            }
            i = j;
        }
        return result;
    }

    private Comparator<GroupStanding> comparatorForStage(int stage, List<GroupStanding> cluster, List<Match> matches) {
        switch (stage) {
            case STAGE_POINTS:
                return Comparator.comparingInt(GroupStanding::getPoints).reversed();
            case STAGE_GOAL_DIFFERENCE:
                return Comparator.comparingInt(GroupStanding::getGoalDifference).reversed();
            case STAGE_GOALS_FOR:
                return Comparator.comparingInt(GroupStanding::getGoalsFor).reversed();
            case STAGE_HEAD_TO_HEAD:
                return headToHeadComparator(cluster, matches);
            case STAGE_FAIR_PLAY:
                // Higher (closer to 0) fair-play points = fewer cards = ranks higher.
                return Comparator.comparingInt(GroupStanding::getFairPlayPoints).reversed();
            case STAGE_TIEBREAK:
                // Manual penalty-shootout order: lower rank wins, unset (null) sorts last.
                return Comparator.comparing(GroupStanding::getTiebreakRank,
                        Comparator.nullsLast(Comparator.naturalOrder()));
            case STAGE_TEAM_NAME:
            default:
                return Comparator.comparing(s -> s.getTeam().getTeamName(),
                        Comparator.nullsLast(Comparator.naturalOrder()));
        }
    }

    /**
     * Build a head-to-head comparator from a mini-table of only the matches
     * played among the teams in {@code cluster}. Orders by head-to-head points,
     * then head-to-head goal difference, then head-to-head goals for.
     */
    private Comparator<GroupStanding> headToHeadComparator(List<GroupStanding> cluster, List<Match> matches) {
        Set<Long> clusterTeamIds = cluster.stream()
                .map(s -> s.getTeam().getId())
                .collect(Collectors.toCollection(HashSet::new));

        // teamId -> [points, goalDifference, goalsFor] within the cluster
        Map<Long, int[]> h2h = new HashMap<>();
        clusterTeamIds.forEach(id -> h2h.put(id, new int[3]));

        for (Match match : matches) {
            Long homeId = match.getHomeTeam().getId();
            Long awayId = match.getAwayTeam().getId();
            if (!clusterTeamIds.contains(homeId) || !clusterTeamIds.contains(awayId)) {
                continue;
            }
            int homeScore = match.getHomeTeamScore() != null ? match.getHomeTeamScore() : 0;
            int awayScore = match.getAwayTeamScore() != null ? match.getAwayTeamScore() : 0;

            int[] home = h2h.get(homeId);
            int[] away = h2h.get(awayId);
            home[2] += homeScore;
            away[2] += awayScore;
            home[1] += (homeScore - awayScore);
            away[1] += (awayScore - homeScore);
            if (homeScore > awayScore) {
                home[0] += 3;
            } else if (homeScore < awayScore) {
                away[0] += 3;
            } else {
                home[0] += 1;
                away[0] += 1;
            }
        }

        return Comparator
                .comparingInt((GroupStanding s) -> h2h.get(s.getTeam().getId())[0]).reversed()
                .thenComparing(Comparator.comparingInt((GroupStanding s) -> h2h.get(s.getTeam().getId())[1]).reversed())
                .thenComparing(Comparator.comparingInt((GroupStanding s) -> h2h.get(s.getTeam().getId())[2]).reversed());
    }

    private void updateStandingsForMatch(List<GroupStanding> standings, Match match) {
        GroupStanding homeStanding = standings.stream()
                .filter(s -> s.getTeam().getId().equals(match.getHomeTeam().getId()))
                .findFirst()
                .orElse(null);

        GroupStanding awayStanding = standings.stream()
                .filter(s -> s.getTeam().getId().equals(match.getAwayTeam().getId()))
                .findFirst()
                .orElse(null);

        if (homeStanding != null && awayStanding != null) {
            int homeScore = match.getHomeTeamScore() != null ? match.getHomeTeamScore() : 0;
            int awayScore = match.getAwayTeamScore() != null ? match.getAwayTeamScore() : 0;

            // Update matches played
            homeStanding.setMatchesPlayed(homeStanding.getMatchesPlayed() + 1);
            awayStanding.setMatchesPlayed(awayStanding.getMatchesPlayed() + 1);

            // Update goals
            homeStanding.setGoalsFor(homeStanding.getGoalsFor() + homeScore);
            homeStanding.setGoalsAgainst(homeStanding.getGoalsAgainst() + awayScore);
            awayStanding.setGoalsFor(awayStanding.getGoalsFor() + awayScore);
            awayStanding.setGoalsAgainst(awayStanding.getGoalsAgainst() + homeScore);

            // Update goal difference
            homeStanding.setGoalDifference(homeStanding.getGoalsFor() - homeStanding.getGoalsAgainst());
            awayStanding.setGoalDifference(awayStanding.getGoalsFor() - awayStanding.getGoalsAgainst());

            // Update wins/draws/losses and points
            if (homeScore > awayScore) {
                homeStanding.setWins(homeStanding.getWins() + 1);
                homeStanding.setPoints(homeStanding.getPoints() + 3);
                awayStanding.setLosses(awayStanding.getLosses() + 1);
            } else if (homeScore < awayScore) {
                awayStanding.setWins(awayStanding.getWins() + 1);
                awayStanding.setPoints(awayStanding.getPoints() + 3);
                homeStanding.setLosses(homeStanding.getLosses() + 1);
            } else {
                homeStanding.setDraws(homeStanding.getDraws() + 1);
                homeStanding.setPoints(homeStanding.getPoints() + 1);
                awayStanding.setDraws(awayStanding.getDraws() + 1);
                awayStanding.setPoints(awayStanding.getPoints() + 1);
            }
        }
    }

    private RoundGroupResponse convertToGroupResponse(RoundGroup group) {
        List<TeamSimpleResponse> teams = roundGroupTeamRepository.findByGroupId(group.getId())
                .stream()
                .map(this::convertToTeamSimpleResponse)
                .collect(Collectors.toList());

        // Order by the persisted position (computed via the full ranking sequence
        // in recalculateGroupStandings); falls back to ranking criteria for nulls.
        List<GroupStandingResponse> standings = groupStandingRepository
                .findByGroupIdOrderByPosition(group.getId())
                .stream()
                .map(this::convertToStandingResponse)
                .collect(Collectors.toList());

        long totalMatches = matchRepository.countByGroupId(group.getId());
        long completedMatches = matchRepository.countCompletedByGroupId(group.getId());

        List<RoundGroupResponse> childGroups = group.getChildGroups().stream()
                .map(this::convertToGroupResponse)
                .collect(Collectors.toList());

        return RoundGroupResponse.builder()
                .id(group.getId())
                .roundId(group.getRound().getId())
                .parentGroupId(group.getParentGroup() != null ? group.getParentGroup().getId() : null)
                .groupName(group.getGroupName())
                .groupFormat(group.getGroupFormat() != null ? group.getGroupFormat().toString() : null)
                .advancementRule(group.getAdvancementRule())
                .maxTeams(group.getMaxTeams())
                .status(group.getStatus() != null ? group.getStatus().toString() : null)
                .teams(teams)
                .standings(standings.isEmpty() ? null : standings)
                .childGroups(childGroups.isEmpty() ? null : childGroups)
                .totalMatches((int) totalMatches)
                .completedMatches((int) completedMatches)
                .build();
    }

    private TeamSimpleResponse convertToTeamSimpleResponse(RoundGroupTeam roundGroupTeam) {
        return TeamSimpleResponse.builder()
                .id(roundGroupTeam.getTeam() != null ? roundGroupTeam.getTeam().getId() : null)
                .teamName(roundGroupTeam.getTeam() != null ? roundGroupTeam.getTeam().getTeamName() : null)
                .isPlaceholder(roundGroupTeam.getIsPlaceholder())
                .placeholderName(roundGroupTeam.getPlaceholderName())
                .assignmentType(roundGroupTeam.getAssignmentType() != null ?
                        roundGroupTeam.getAssignmentType().toString() : null)
                .build();
    }

    private GroupStandingResponse convertToStandingResponse(GroupStanding standing) {
        return GroupStandingResponse.builder()
                .id(standing.getId())
                .groupId(standing.getGroup().getId())
                .teamId(standing.getTeam().getId())
                .teamName(standing.getTeam().getTeamName())
                .matchesPlayed(standing.getMatchesPlayed())
                .wins(standing.getWins())
                .draws(standing.getDraws())
                .losses(standing.getLosses())
                .goalsFor(standing.getGoalsFor())
                .goalsAgainst(standing.getGoalsAgainst())
                .goalDifference(standing.getGoalDifference())
                .points(standing.getPoints())
                .yellowCards(standing.getYellowCards())
                .redCards(standing.getRedCards())
                .fairPlayPoints(standing.getFairPlayPoints())
                .tiebreakRank(standing.getTiebreakRank())
                .position(standing.getPosition())
                .isAdvanced(standing.getIsAdvanced())
                .build();
    }

    @Override
    public List<MatchResponse> generateGroupMatches(Long groupId, GroupMatchGenerationRequest request) {
        log.info("Generating matches for group ID: {}", groupId);

        RoundGroup group = roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Get all non-placeholder teams in the group
        List<RoundGroupTeam> groupTeams = roundGroupTeamRepository.findByGroupId(groupId);
        List<Team> teams = groupTeams.stream()
                .filter(rgt -> !rgt.getIsPlaceholder() && rgt.getTeam() != null)
                .map(RoundGroupTeam::getTeam)
                .collect(Collectors.toList());

        if (teams.size() < 2) {
            throw new RoundServiceException("At least 2 teams required to generate matches",
                    HttpStatus.BAD_REQUEST);
        }

        // Check if matches already exist
        long existingMatches = matchRepository.countByGroupId(groupId);
        if (existingMatches > 0) {
            throw new RoundServiceException("Matches already exist for this group. Clear them first.",
                    HttpStatus.CONFLICT);
        }

        // Set defaults
        int matchTimeGap = request.getMatchTimeGapMinutes() != null ?
                request.getMatchTimeGapMinutes() : 180; // 3 hours default
        int matchDuration = request.getMatchDurationMinutes() != null ?
                request.getMatchDurationMinutes() : 90;

        Venue venue = null;
        if (request.getVenueId() != null) {
            venue = venueRepository.findById(request.getVenueId())
                    .orElse(null);
        }

        List<Match> matches = new ArrayList<>();
        LocalDateTime currentMatchDate = request.getStartDate();
        int matchOrder = 1;

        // Generate matches based on fixture format
        FixtureFormat format = request.getFixtureFormat();
        boolean doubleRoundRobin = format == FixtureFormat.DOUBLE_ROUND_ROBIN;
        
        // For groups, only ROUND_ROBIN formats are supported
        if (format != FixtureFormat.ROUND_ROBIN && format != FixtureFormat.DOUBLE_ROUND_ROBIN) {
            throw new RoundServiceException(
                    "Group match generation only supports ROUND_ROBIN or DOUBLE_ROUND_ROBIN formats",
                    HttpStatus.BAD_REQUEST);
        }

        // Generate round-robin matches
        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Team homeTeam = teams.get(i);
                Team awayTeam = teams.get(j);

                // First match (home vs away)
                Match match1 = createMatch(
                        group,
                        homeTeam,
                        awayTeam,
                        currentMatchDate,
                        matchDuration,
                        venue,
                        matchOrder++
                );
                matches.add(match1);
                // Next match starts after current match ends + gap
                currentMatchDate = currentMatchDate.plusMinutes(matchDuration).plusMinutes(matchTimeGap);

                // Second match (away vs home) if double round-robin
                if (doubleRoundRobin) {
                    Match match2 = createMatch(
                            group,
                            awayTeam,
                            homeTeam,
                            currentMatchDate,
                            matchDuration,
                            venue,
                            matchOrder++
                    );
                    matches.add(match2);
                    // Next match starts after current match ends + gap
                    currentMatchDate = currentMatchDate.plusMinutes(matchDuration).plusMinutes(matchTimeGap);
                }
            }
        }

        List<Match> savedMatches = matchRepository.saveAll(matches);
        log.info("Generated {} matches for group ID: {}", savedMatches.size(), groupId);

        // Convert to MatchResponse DTOs to avoid circular reference issues
        return savedMatches.stream()
                .map(this::convertToMatchResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> getGroupMatches(Long groupId) {
        log.info("Fetching all matches for group ID: {}", groupId);

        // Verify group exists
        roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<Match> matches = matchRepository.findByGroupId(groupId);

        // Convert to MatchResponse DTOs to avoid circular reference issues
        return matches.stream()
                .map(this::convertToMatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Match entity to MatchResponse DTO
     */
    private MatchResponse convertToMatchResponse(Match match) {
        // Get roundNumber from TournamentRound if available, otherwise use legacyRound
        Integer roundNumber = null;
        if (match.getRound() != null) {
            roundNumber = match.getRound().getRoundNumber();
        } else if (match.getLegacyRound() != null) {
            roundNumber = match.getLegacyRound();
        }
        
        // Get groupName from group relationship if groupName column is null
        String groupName = match.getGroupName();
        if (groupName == null && match.getGroup() != null) {
            groupName = match.getGroup().getGroupName();
        }
        
        return MatchResponse.builder()
                .id(match.getId())
                .tournamentId(match.getTournament().getId())
                .tournamentName(match.getTournament().getName())
                .homeTeamId(match.getHomeTeam().getId())
                .homeTeamName(match.getHomeTeam().getTeamName())
                .awayTeamId(match.getAwayTeam().getId())
                .awayTeamName(match.getAwayTeam().getTeamName())
                .venueId(match.getVenue() != null ? match.getVenue().getId() : null)
                .venueName(match.getVenue() != null ? match.getVenue().getName() : null)
                .matchDate(match.getMatchDate())
                .matchStatus(match.getMatchStatus().toString())
                .matchOrder(match.getMatchOrder())
                .round(match.getLegacyRound())  // Keep legacy round for backward compatibility
                .roundNumber(roundNumber)  // Add roundNumber from TournamentRound or legacyRound
                .groupName(groupName)  // Use groupName from column or group relationship
                .homeTeamScore(match.getHomeTeamScore())
                .awayTeamScore(match.getAwayTeamScore())
                .matchDurationMinutes(match.getMatchDurationMinutes())
                .elapsedTimeSeconds(match.getElapsedTimeSeconds())
                .startedAt(match.getStartedAt())
                .completedAt(match.getCompletedAt())
                .createdDate(match.getCreatedDate())
                .updatedDate(match.getUpdatedDate())
                .build();
    }

    @Override
    public void clearGroupMatches(Long groupId) {
        log.info("Clearing all matches for group ID: {}", groupId);

        // Verify group exists
        roundGroupRepository.findById(groupId)
                .orElseThrow(() -> new RoundServiceException(GROUP_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Get all matches for this group
        List<Match> matches = matchRepository.findByGroupId(groupId);

        if (matches.isEmpty()) {
            log.info("No matches found to clear for group ID: {}", groupId);
            return;
        }

        // Delete all matches
        matchRepository.deleteAll(matches);
        log.info("Cleared {} matches for group ID: {}", matches.size(), groupId);
    }

    private Match createMatch(RoundGroup group, Team homeTeam, Team awayTeam,
                             LocalDateTime matchDate, int durationMinutes,
                             Venue venue, int matchOrder) {
        TournamentRound round = group.getRound();
        return Match.builder()
                .tournament(round.getTournament())
                .round(round)
                .group(group)
                .groupName(group.getGroupName())  // Set groupName from group
                .legacyRound(round.getRoundNumber())  // Set legacyRound from roundNumber for backward compatibility
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchDate(matchDate)
                .matchStatus(MatchStatus.SCHEDULED)
                .matchDurationMinutes(durationMinutes)
                .venue(venue)
                .matchOrder(matchOrder)
                .homeTeamScore(0)
                .awayTeamScore(0)
                .elapsedTimeSeconds(0)
                .isPlaceholderMatch(false)
                .build();
    }
}
