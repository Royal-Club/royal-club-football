package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.*;
import com.bjit.royalclub.royalclubfootball.enums.GroupFormat;
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
import java.util.List;
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

        RoundGroup group = RoundGroup.builder()
                .round(round)
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

        List<RoundGroup> groups = roundGroupRepository.findByRoundId(roundId);

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

        List<GroupStanding> standings = groupStandingRepository
                .findByGroupIdOrderByRankingCriteria(groupId);

        // Update positions
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setPosition(i + 1);
        }

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

        // Reset all standings
        for (GroupStanding standing : standings) {
            standing.setMatchesPlayed(0);
            standing.setWins(0);
            standing.setDraws(0);
            standing.setLosses(0);
            standing.setGoalsFor(0);
            standing.setGoalsAgainst(0);
            standing.setGoalDifference(0);
            standing.setPoints(0);
        }

        // Recalculate from completed matches
        List<Match> matches = matchRepository.findCompletedByGroupId(groupId);

        for (Match match : matches) {
            updateStandingsForMatch(standings, match);
        }

        // Save updated standings
        groupStandingRepository.saveAll(standings);

        // Update positions
        standings = groupStandingRepository.findByGroupIdOrderByRankingCriteria(groupId);
        for (int i = 0; i < standings.size(); i++) {
            standings.get(i).setPosition(i + 1);
        }
        groupStandingRepository.saveAll(standings);

        log.info("Group standings recalculated successfully for group ID: {}", groupId);
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

        List<GroupStandingResponse> standings = groupStandingRepository
                .findByGroupIdOrderByRankingCriteria(group.getId())
                .stream()
                .map(this::convertToStandingResponse)
                .collect(Collectors.toList());

        long totalMatches = matchRepository.countByGroupId(group.getId());
        long completedMatches = matchRepository.countCompletedByGroupId(group.getId());

        return RoundGroupResponse.builder()
                .id(group.getId())
                .roundId(group.getRound().getId())
                .groupName(group.getGroupName())
                .groupFormat(group.getGroupFormat() != null ? group.getGroupFormat().toString() : null)
                .advancementRule(group.getAdvancementRule())
                .maxTeams(group.getMaxTeams())
                .status(group.getStatus() != null ? group.getStatus().toString() : null)
                .teams(teams)
                .standings(standings.isEmpty() ? null : standings)
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
        boolean doubleRoundRobin = request.getDoubleRoundRobin() != null ?
                request.getDoubleRoundRobin() : false;

        Venue venue = null;
        if (request.getVenueId() != null) {
            venue = venueRepository.findById(request.getVenueId())
                    .orElse(null);
        }

        List<Match> matches = new ArrayList<>();
        LocalDateTime currentMatchDate = request.getStartDate();
        int matchOrder = 1;

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
                currentMatchDate = currentMatchDate.plusMinutes(matchTimeGap);

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
                    currentMatchDate = currentMatchDate.plusMinutes(matchTimeGap);
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
                .round(match.getLegacyRound())
                .groupName(match.getGroupName())
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
        return Match.builder()
                .tournament(group.getRound().getTournament())
                .round(group.getRound())
                .group(group)
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
