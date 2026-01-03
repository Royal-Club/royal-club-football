package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.*;
import com.bjit.royalclub.royalclubfootball.enums.*;
import com.bjit.royalclub.royalclubfootball.exception.RoundServiceException;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.model.*;
import com.bjit.royalclub.royalclubfootball.repository.*;
import com.bjit.royalclub.royalclubfootball.service.LogicNodeService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TournamentRoundServiceImpl implements TournamentRoundService {

    private final TournamentRoundRepository tournamentRoundRepository;
    private final RoundGroupRepository roundGroupRepository;
    private final RoundGroupTeamRepository roundGroupTeamRepository;
    private final RoundTeamRepository roundTeamRepository;
    private final GroupStandingRepository groupStandingRepository;
    private final AdvancementRuleRepository advancementRuleRepository;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final VenueRepository venueRepository;
    private final LogicNodeService logicNodeService;

    @Override
    public TournamentRoundResponse createRound(TournamentRoundRequest request) {
        log.info("Creating round for tournament ID: {}", request.getTournamentId());

        Tournament tournament = tournamentRepository.findById(request.getTournamentId())
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Validate round number uniqueness
        if (tournamentRoundRepository.existsByTournamentIdAndRoundNumber(
                request.getTournamentId(), request.getRoundNumber())) {
            throw new RoundServiceException(ROUND_NUMBER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        // Auto-calculate sequenceOrder to prevent duplicates and race conditions
        // Get the maximum sequenceOrder for this tournament and add 1
        List<TournamentRound> existingRounds = tournamentRoundRepository.findByTournamentIdOrderBySequence(request.getTournamentId());
        int maxSequenceOrder = existingRounds.stream()
                .mapToInt(TournamentRound::getSequenceOrder)
                .max()
                .orElse(0);
        int calculatedSequenceOrder = maxSequenceOrder + 1;
        
        // If frontend provided sequenceOrder, validate it matches our calculation (for safety)
        // But use our calculated value to ensure uniqueness
        if (request.getSequenceOrder() != null && request.getSequenceOrder() != calculatedSequenceOrder) {
            log.warn("Frontend provided sequenceOrder {} but calculated {}, using calculated value to ensure uniqueness", 
                    request.getSequenceOrder(), calculatedSequenceOrder);
        }

        TournamentRound round = TournamentRound.builder()
                .tournament(tournament)
                .roundNumber(request.getRoundNumber())
                .roundName(request.getRoundName())
                .roundType(RoundType.valueOf(request.getRoundType()))
                .advancementRule(request.getAdvancementRule())
                .status(RoundStatus.NOT_STARTED)
                .sequenceOrder(calculatedSequenceOrder) // Use calculated value instead of request value
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        TournamentRound savedRound = tournamentRoundRepository.save(round);
        log.info("Round created successfully with ID: {}, sequenceOrder: {}", savedRound.getId(), savedRound.getSequenceOrder());

        return convertToRoundResponse(savedRound);
    }

    @Override
    public TournamentRoundResponse updateRound(Long roundId, TournamentRoundRequest request) {
        log.info("Updating round ID: {}", roundId);

        TournamentRound round = tournamentRoundRepository.findById(roundId)
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check if round number is being changed and if new number already exists
        if (!round.getRoundNumber().equals(request.getRoundNumber()) &&
                tournamentRoundRepository.existsByTournamentIdAndRoundNumber(
                        request.getTournamentId(), request.getRoundNumber())) {
            throw new RoundServiceException(ROUND_NUMBER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        // Check if sequence order is being changed and if new order already exists
        if (!round.getSequenceOrder().equals(request.getSequenceOrder()) &&
                tournamentRoundRepository.existsByTournamentIdAndSequenceOrder(
                        request.getTournamentId(), request.getSequenceOrder())) {
            throw new RoundServiceException(INVALID_ROUND_SEQUENCE, HttpStatus.CONFLICT);
        }

        round.setRoundNumber(request.getRoundNumber());
        round.setRoundName(request.getRoundName());
        round.setRoundType(RoundType.valueOf(request.getRoundType()));
        round.setAdvancementRule(request.getAdvancementRule());
        round.setSequenceOrder(request.getSequenceOrder());
        round.setStartDate(request.getStartDate());
        round.setEndDate(request.getEndDate());

        TournamentRound updatedRound = tournamentRoundRepository.save(round);
        log.info("Round updated successfully with ID: {}", updatedRound.getId());

        return convertToRoundResponse(updatedRound);
    }

    @Override
    public void deleteRound(Long roundId) {
        log.info("Deleting round ID: {}", roundId);

        TournamentRound round = tournamentRoundRepository.findById(roundId)
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check if round has matches
        long matchCount = matchRepository.countByRoundId(roundId);
        if (matchCount > 0) {
            throw new RoundServiceException(ROUND_HAS_MATCHES, HttpStatus.CONFLICT);
        }

        tournamentRoundRepository.delete(round);
        log.info("Round deleted successfully with ID: {}", roundId);
    }

    @Override
    public TournamentRoundResponse getRoundById(Long roundId) {
        log.info("Fetching round by ID: {}", roundId);

        TournamentRound round = tournamentRoundRepository.findById(roundId)
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        return convertToRoundResponse(round);
    }

    @Override
    public List<TournamentRoundResponse> getRoundsByTournamentId(Long tournamentId) {
        log.info("Fetching all rounds for tournament ID: {}", tournamentId);

        List<TournamentRound> rounds = tournamentRoundRepository.findByTournamentIdOrderBySequence(tournamentId);

        return rounds.stream()
                .map(this::convertToRoundResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TournamentStructureResponse getTournamentStructure(Long tournamentId) {
        log.info("Fetching tournament structure for tournament ID: {}", tournamentId);

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        List<TournamentRound> rounds = tournamentRoundRepository.findByTournamentIdOrderBySequence(tournamentId);

        List<TournamentRoundResponse> roundResponses = rounds.stream()
                .map(this::convertToRoundResponse)
                .collect(Collectors.toList());

        long totalMatches = matchRepository.countByTournamentId(tournamentId);
        long completedMatches = matchRepository.countCompletedByTournamentId(tournamentId);

        return TournamentStructureResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .sportType(tournament.getSportType() != null ? tournament.getSportType().toString() : null)
                .tournamentType(tournament.getTournamentType() != null ? tournament.getTournamentType().toString() : null)
                .status(tournament.getTournamentStatus() != null ? tournament.getTournamentStatus().toString() : null)
                .rounds(roundResponses)
                .totalRounds(rounds.size())
                .totalMatches((int) totalMatches)
                .completedMatches((int) completedMatches)
                .build();
    }

    @Override
    public AdvancedTeamsResponse completeRound(RoundCompletionRequest request) {
        log.info("Completing round ID: {}", request.getRoundId());

        TournamentRound round = tournamentRoundRepository.findById(request.getRoundId())
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Check if all matches in the round are completed (handles both GROUP_BASED and DIRECT_KNOCKOUT)
        boolean allMatchesCompleted = areAllMatchesCompletedInRound(round);
        if (!allMatchesCompleted) {
            throw new RoundServiceException(ROUND_NOT_COMPLETED, HttpStatus.BAD_REQUEST);
        }

        // Recalculate standings if requested (default: true)
        if (request.getRecalculateStandings() == null || request.getRecalculateStandings()) {
            recalculateGroupStandings(round);
        }

        // Update round status
        round.setStatus(RoundStatus.COMPLETED);
        tournamentRoundRepository.save(round);

        // Advance teams manually (selectedTeamIds is required)
        AdvancedTeamsResponse advancedTeamsResponse = null;
        if (request.getSelectedTeamIds() != null && !request.getSelectedTeamIds().isEmpty()) {
            advancedTeamsResponse = advanceSelectedTeamsToNextRound(round, request.getSelectedTeamIds());
        } else {
            // No teams selected - round is completed but no advancement
            log.info("Round completed without team advancement (no teams selected)");
        }

        log.info("Round completed successfully with ID: {}", request.getRoundId());

        return advancedTeamsResponse;
    }

    @Override
    public TournamentRoundResponse startRound(Long roundId) {
        TournamentRound round = tournamentRoundRepository.findById(roundId)
                .orElseThrow(() -> new RoundServiceException(
                        String.format("Round with ID %d is not found", roundId),
                        HttpStatus.NOT_FOUND));

        // Validate round can be started
        if (round.getStatus() != RoundStatus.NOT_STARTED) {
            throw new RoundServiceException(
                    String.format("Round is already %s. Only NOT_STARTED rounds can be started.", round.getStatus()),
                    HttpStatus.CONFLICT);
        }

        // Check if previous round is completed (if not the first round)
        if (round.getSequenceOrder() > 1) {
            List<TournamentRound> previousRounds = tournamentRoundRepository
                    .findPreviousRoundBySequence(round.getTournament().getId(), round.getSequenceOrder());
            TournamentRound previousRound = previousRounds.isEmpty() ? null : previousRounds.get(0);

            if (previousRound != null && previousRound.getStatus() != RoundStatus.COMPLETED) {
                throw new RoundServiceException(
                        String.format("Cannot start round '%s'. Previous round '%s' (Sequence %d) must be completed first.",
                                round.getRoundName(), previousRound.getRoundName(), previousRound.getSequenceOrder()),
                        HttpStatus.BAD_REQUEST);
            }
        }

        // Ensure matches have been generated before starting the round
        if (round.getRoundType() == RoundType.DIRECT_KNOCKOUT) {
            // For DIRECT_KNOCKOUT, check matches directly in the round
            long matchCount = matchRepository.countByRoundId(roundId);
            if (matchCount == 0) {
                throw new RoundServiceException(
                        String.format("Cannot start round '%s'. You must generate matches for this DIRECT_KNOCKOUT round before starting it. Please use the 'Generate Matches' button to create the fixtures first.",
                                round.getRoundName()),
                        HttpStatus.BAD_REQUEST);
            }
        } else if (round.getRoundType() == RoundType.GROUP_BASED) {
            // For GROUP_BASED, check if any group has matches
            List<RoundGroup> groups = roundGroupRepository.findByRoundId(roundId);

            if (groups.isEmpty()) {
                throw new RoundServiceException(
                        String.format("Cannot start round '%s'. You must create groups for this GROUP_BASED round before starting it.",
                                round.getRoundName()),
                        HttpStatus.BAD_REQUEST);
            }

            // Check if at least one group has matches
            boolean hasMatches = false;
            for (RoundGroup group : groups) {
                long groupMatchCount = matchRepository.countByGroupId(group.getId());
                if (groupMatchCount > 0) {
                    hasMatches = true;
                    break;
                }
            }

            if (!hasMatches) {
                throw new RoundServiceException(
                        String.format("Cannot start round '%s'. You must generate matches for the groups in this round before starting it. Please use the 'Generate Matches' button on each group to create the fixtures first.",
                                round.getRoundName()),
                        HttpStatus.BAD_REQUEST);
            }
        }

        // Update round status to ONGOING
        round.setStatus(RoundStatus.ONGOING);
        tournamentRoundRepository.save(round);

        log.info("Round started successfully with ID: {}", roundId);

        return convertToRoundResponse(round);
    }

    @Override
    public TournamentRoundResponse getNextRound(Long tournamentId, Integer currentSequenceOrder) {
        log.info("Fetching next round for tournament ID: {} after sequence: {}", tournamentId, currentSequenceOrder);

        return tournamentRoundRepository.findNextRoundBySequence(tournamentId, currentSequenceOrder)
                .map(this::convertToRoundResponse)
                .orElse(null);
    }

    @Override
    public TournamentRoundResponse getPreviousRound(Long tournamentId, Integer currentSequenceOrder) {
        log.info("Fetching previous round for tournament ID: {} before sequence: {}", tournamentId, currentSequenceOrder);

        List<TournamentRound> previousRounds = tournamentRoundRepository.findPreviousRoundBySequence(tournamentId, currentSequenceOrder);
        if (previousRounds.isEmpty()) {
            return null;
        }
        // Return the first round (ordered by id ASC as tiebreaker)
        return convertToRoundResponse(previousRounds.get(0));
    }

    /**
     * Check if all matches in a round are completed
     * For GROUP_BASED rounds: checks all matches in all groups
     * For DIRECT_KNOCKOUT rounds: checks all matches directly in the round
     */
    private boolean areAllMatchesCompletedInRound(TournamentRound round) {
        if (round.getRoundType() == RoundType.GROUP_BASED) {
            // For GROUP_BASED rounds, check all matches in all groups
            List<RoundGroup> groups = roundGroupRepository.findByRoundId(round.getId());
            if (groups.isEmpty()) {
                return false; // No groups means no matches
            }
            
            for (RoundGroup group : groups) {
                boolean allGroupMatchesCompleted = matchRepository.areAllMatchesCompletedInGroup(group.getId());
                if (!allGroupMatchesCompleted) {
                    return false;
                }
            }
            return true;
        } else {
            // For DIRECT_KNOCKOUT rounds, check matches directly in the round
            return matchRepository.areAllMatchesCompletedInRound(round.getId());
        }
    }

    private void recalculateGroupStandings(TournamentRound round) {
        log.info("Recalculating group standings for round ID: {}", round.getId());

        if (round.getRoundType() != RoundType.GROUP_BASED) {
            log.info("Round is not group-based, skipping standings calculation");
            return;
        }

        List<RoundGroup> groups = roundGroupRepository.findByRoundId(round.getId());

        for (RoundGroup group : groups) {
            List<GroupStanding> standings = groupStandingRepository.findByGroupId(group.getId());

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

            // Recalculate from matches
            List<Match> matches = matchRepository.findCompletedByGroupId(group.getId());

            for (Match match : matches) {
                updateStandingsForMatch(standings, match);
            }

            // Calculate positions and save
            standings = groupStandingRepository.findByGroupIdOrderByRankingCriteria(group.getId());
            for (int i = 0; i < standings.size(); i++) {
                standings.get(i).setPosition(i + 1);
            }

            groupStandingRepository.saveAll(standings);
        }

        log.info("Group standings recalculated successfully for round ID: {}", round.getId());
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

    private AdvancedTeamsResponse advanceTeamsToNextRound(TournamentRound sourceRound) {
        log.info("Advancing teams from round ID: {} using automatic rules", sourceRound.getId());

        // Find next round
        TournamentRound targetRound = tournamentRoundRepository
                .findNextRoundBySequence(sourceRound.getTournament().getId(), sourceRound.getSequenceOrder())
                .orElse(null);

        if (targetRound == null) {
            log.info("No target round found for advancement");
            return null;
        }

        List<AdvancedTeamsResponse.TeamAdvancementInfo> advancedTeams = new ArrayList<>();

        // Find advancement rules
        List<AdvancementRule> rules = advancementRuleRepository
                .findBySourceRoundIdOrderByPriority(sourceRound.getId());

        for (AdvancementRule rule : rules) {
            List<Team> teamsToAdvance = determineTeamsToAdvance(rule, sourceRound);

            for (Team team : teamsToAdvance) {
                // Assign team to target round/group based on rule configuration
                assignTeamToTargetRound(team, targetRound, rule);

                advancedTeams.add(AdvancedTeamsResponse.TeamAdvancementInfo.builder()
                        .teamId(team.getId())
                        .teamName(team.getTeamName())
                        .advancementReason(rule.getRuleType().toString())
                        .build());
            }
        }

        log.info("Advanced {} teams to next round", advancedTeams.size());

        return AdvancedTeamsResponse.builder()
                .sourceRoundId(sourceRound.getId())
                .sourceRoundName(sourceRound.getRoundName())
                .targetRoundId(targetRound.getId())
                .targetRoundName(targetRound.getRoundName())
                .teamsAdvanced(advancedTeams.size())
                .teams(advancedTeams)
                .build();
    }

    /**
     * Advance manually selected teams to next round
     */
    private AdvancedTeamsResponse advanceSelectedTeamsToNextRound(TournamentRound sourceRound, List<Long> selectedTeamIds) {
        log.info("Advancing {} manually selected teams from round ID: {}", selectedTeamIds.size(), sourceRound.getId());

        // Find next round
        TournamentRound targetRound = tournamentRoundRepository
                .findNextRoundBySequence(sourceRound.getTournament().getId(), sourceRound.getSequenceOrder())
                .orElse(null);

        if (targetRound == null) {
            log.info("No target round found for advancement");
            return null;
        }

        List<AdvancedTeamsResponse.TeamAdvancementInfo> advancedTeams = new ArrayList<>();

        // Get teams from selected IDs
        List<Team> teamsToAdvance = teamRepository.findAllById(selectedTeamIds);

        if (teamsToAdvance.size() != selectedTeamIds.size()) {
            throw new RoundServiceException(
                    "Some selected teams were not found",
                    HttpStatus.BAD_REQUEST);
        }

        // Assign teams to target round
        for (Team team : teamsToAdvance) {
            if (targetRound.getRoundType() == RoundType.GROUP_BASED) {
                // For group-based rounds, assign to first group or let admin assign later
                // For now, we'll assign to the first group if available
                List<RoundGroup> groups = roundGroupRepository.findByRoundId(targetRound.getId());
                if (!groups.isEmpty()) {
                    RoundGroup firstGroup = groups.get(0);
                    RoundGroupTeam roundGroupTeam = RoundGroupTeam.builder()
                            .group(firstGroup)
                            .team(team)
                            .assignmentType(TeamAssignmentType.MANUAL)
                            .isPlaceholder(false)
                            .build();
                    roundGroupTeamRepository.save(roundGroupTeam);
                } else {
                    // No groups yet, create a round team entry
                    RoundTeam roundTeam = RoundTeam.builder()
                            .round(targetRound)
                            .team(team)
                            .assignmentType(TeamAssignmentType.MANUAL)
                            .isPlaceholder(false)
                            .build();
                    roundTeamRepository.save(roundTeam);
                }
            } else {
                // Direct knockout round
                RoundTeam roundTeam = RoundTeam.builder()
                        .round(targetRound)
                        .team(team)
                        .assignmentType(TeamAssignmentType.MANUAL)
                        .isPlaceholder(false)
                        .build();
                roundTeamRepository.save(roundTeam);
            }

            advancedTeams.add(AdvancedTeamsResponse.TeamAdvancementInfo.builder()
                    .teamId(team.getId())
                    .teamName(team.getTeamName())
                    .advancementReason("MANUAL_SELECTION")
                    .build());
        }

        log.info("Advanced {} manually selected teams to next round", advancedTeams.size());

        return AdvancedTeamsResponse.builder()
                .sourceRoundId(sourceRound.getId())
                .sourceRoundName(sourceRound.getRoundName())
                .targetRoundId(targetRound.getId())
                .targetRoundName(targetRound.getRoundName())
                .teamsAdvanced(advancedTeams.size())
                .teams(advancedTeams)
                .build();
    }

    private List<Team> determineTeamsToAdvance(AdvancementRule rule, TournamentRound sourceRound) {
        List<Team> teams = new ArrayList<>();

        // Implementation depends on rule type
        // For now, returning empty list - will be implemented based on specific rule logic

        return teams;
    }

    private void assignTeamToTargetRound(Team team, TournamentRound targetRound, AdvancementRule rule) {
        if (targetRound.getRoundType() == RoundType.GROUP_BASED && rule.getTargetGroup() != null) {
            // Assign to group
            RoundGroupTeam roundGroupTeam = RoundGroupTeam.builder()
                    .group(rule.getTargetGroup())
                    .team(team)
                    .assignmentType(TeamAssignmentType.RULE_BASED)
                    .sourceRule(rule.getRuleConfig())
                    .isPlaceholder(false)
                    .build();
            roundGroupTeamRepository.save(roundGroupTeam);
        } else {
            // Assign directly to round
            RoundTeam roundTeam = RoundTeam.builder()
                    .round(targetRound)
                    .team(team)
                    .assignmentType(TeamAssignmentType.RULE_BASED)
                    .sourceRule(rule.getRuleConfig())
                    .isPlaceholder(false)
                    .build();
            roundTeamRepository.save(roundTeam);
        }
    }

    private TournamentRoundResponse convertToRoundResponse(TournamentRound round) {
        List<RoundGroupResponse> groups = roundGroupRepository.findByRoundId(round.getId())
                .stream()
                .map(this::convertToGroupResponse)
                .collect(Collectors.toList());

        List<TeamSimpleResponse> teams = roundTeamRepository.findByRoundId(round.getId())
                .stream()
                .map(this::convertToTeamSimpleResponse)
                .collect(Collectors.toList());

        // Count matches correctly based on round type
        long totalMatches;
        long completedMatches;
        
        if (round.getRoundType() == RoundType.GROUP_BASED) {
            // For GROUP_BASED rounds, count all matches in all groups
            totalMatches = 0;
            completedMatches = 0;
            List<RoundGroup> roundGroups = roundGroupRepository.findByRoundId(round.getId());
            for (RoundGroup group : roundGroups) {
                totalMatches += matchRepository.countByGroupId(group.getId());
                completedMatches += matchRepository.countCompletedByGroupId(group.getId());
            }
        } else {
            // For DIRECT_KNOCKOUT rounds, count matches directly in the round
            totalMatches = matchRepository.countByRoundId(round.getId());
            completedMatches = matchRepository.countCompletedByRoundId(round.getId());
        }

        return TournamentRoundResponse.builder()
                .id(round.getId())
                .tournamentId(round.getTournament().getId())
                .roundNumber(round.getRoundNumber())
                .roundName(round.getRoundName())
                .roundType(round.getRoundType().toString())
                .advancementRule(round.getAdvancementRule())
                .status(round.getStatus().toString())
                .sequenceOrder(round.getSequenceOrder())
                .startDate(round.getStartDate())
                .endDate(round.getEndDate())
                .groups(groups.isEmpty() ? null : groups)
                .teams(teams.isEmpty() ? null : teams)
                .totalMatches((int) totalMatches)
                .completedMatches((int) completedMatches)
                .build();
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

    private TeamSimpleResponse convertToTeamSimpleResponse(RoundTeam roundTeam) {
        return TeamSimpleResponse.builder()
                .id(roundTeam.getTeam() != null ? roundTeam.getTeam().getId() : null)
                .teamName(roundTeam.getTeam() != null ? roundTeam.getTeam().getTeamName() : null)
                .isPlaceholder(roundTeam.getIsPlaceholder())
                .placeholderName(roundTeam.getPlaceholderName())
                .seedPosition(roundTeam.getSeedPosition())
                .assignmentType(roundTeam.getAssignmentType() != null ?
                        roundTeam.getAssignmentType().toString() : null)
                .build();
    }

    @Override
    @Transactional
    public void assignTeamsToRound(Long roundId, TeamAssignmentRequest request) {
        log.info("Assigning {} teams to round ID: {}", request.getTeamIds().size(), roundId);

        TournamentRound round = tournamentRoundRepository.findById(roundId)
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Only allow assignment to DIRECT_KNOCKOUT rounds
        if (round.getRoundType() != RoundType.DIRECT_KNOCKOUT) {
            throw new RoundServiceException(
                    "Teams can only be assigned directly to DIRECT_KNOCKOUT rounds. Use group assignment for GROUP_BASED rounds.",
                    HttpStatus.BAD_REQUEST);
        }

        // Check if round is already started
        if (round.getStatus() != RoundStatus.NOT_STARTED) {
            throw new RoundServiceException(
                    "Cannot assign teams to a round that has already started",
                    HttpStatus.BAD_REQUEST);
        }

        List<RoundTeam> teamAssignments = new ArrayList<>();

        for (Long teamId : request.getTeamIds()) {
            // Check if team already assigned to this round
            if (roundTeamRepository.existsByRoundIdAndTeamId(roundId, teamId)) {
                log.warn("Team {} is already assigned to round {}", teamId, roundId);
                continue; // Skip already assigned teams
            }

            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RoundServiceException(
                            String.format("Team with ID %d not found", teamId),
                            HttpStatus.NOT_FOUND));

            RoundTeam roundTeam = RoundTeam.builder()
                    .round(round)
                    .team(team)
                    .assignmentType(TeamAssignmentType.MANUAL)
                    .isPlaceholder(false)
                    .build();

            teamAssignments.add(roundTeam);
        }

        if (!teamAssignments.isEmpty()) {
            roundTeamRepository.saveAll(teamAssignments);
            log.info("Assigned {} teams to round ID: {}", teamAssignments.size(), roundId);
        }
    }

    @Override
    @Transactional
    public void removeTeamFromRound(Long roundId, Long teamId) {
        log.info("Removing team ID: {} from round ID: {}", teamId, roundId);

        TournamentRound round = tournamentRoundRepository.findById(roundId)
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Only allow removal from DIRECT_KNOCKOUT rounds
        if (round.getRoundType() != RoundType.DIRECT_KNOCKOUT) {
            throw new RoundServiceException(
                    "Teams can only be removed from DIRECT_KNOCKOUT rounds. Use group team removal for GROUP_BASED rounds.",
                    HttpStatus.BAD_REQUEST);
        }

        // Check if round is already started
        if (round.getStatus() != RoundStatus.NOT_STARTED) {
            throw new RoundServiceException(
                    "Cannot remove teams from a round that has already started",
                    HttpStatus.BAD_REQUEST);
        }

        // Check if team is in round
        if (!roundTeamRepository.existsByRoundIdAndTeamId(roundId, teamId)) {
            throw new RoundServiceException("Team is not in this round", HttpStatus.NOT_FOUND);
        }

        // Check if team has matches in this round
        long matchCount = matchRepository.countByRoundIdAndTeamId(roundId, teamId);
        if (matchCount > 0) {
            throw new RoundServiceException("Cannot remove team with existing matches",
                    HttpStatus.CONFLICT);
        }

        roundTeamRepository.deleteByRoundIdAndTeamId(roundId, teamId);

        log.info("Team removed successfully from round ID: {}", roundId);
    }

    @Override
    @Transactional
    public List<MatchResponse> generateRoundMatches(Long roundId, RoundMatchGenerationRequest request) {
        log.info("Generating matches for round ID: {} with format: {}", roundId, request.getFixtureFormat());

        TournamentRound round = tournamentRoundRepository.findById(roundId)
                .orElseThrow(() -> new RoundServiceException(ROUND_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Only allow for DIRECT_KNOCKOUT rounds
        if (round.getRoundType() != RoundType.DIRECT_KNOCKOUT) {
            throw new RoundServiceException(
                    "Match generation is only available for DIRECT_KNOCKOUT rounds. Use group match generation for GROUP_BASED rounds.",
                    HttpStatus.BAD_REQUEST);
        }

        // Get all non-placeholder teams in the round
        List<RoundTeam> roundTeams = roundTeamRepository.findByRoundId(roundId);
        List<Team> teams = roundTeams.stream()
                .filter(rt -> !rt.getIsPlaceholder() && rt.getTeam() != null)
                .map(RoundTeam::getTeam)
                .collect(Collectors.toList());

        if (teams.size() < 2) {
            throw new RoundServiceException("At least 2 teams required to generate matches",
                    HttpStatus.BAD_REQUEST);
        }

        // Check if matches already exist
        long existingMatches = matchRepository.countByRoundId(roundId);
        if (existingMatches > 0) {
            throw new RoundServiceException("Matches already exist for this round. Clear them first.",
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

        // Generate matches based on fixture format
        FixtureFormat format = request.getFixtureFormat();
        if (format == FixtureFormat.SINGLE_ELIMINATION) {
            matches = generateSingleEliminationMatches(round, teams, currentMatchDate, matchTimeGap, matchDuration, venue);
        } else if (format == FixtureFormat.ROUND_ROBIN) {
            boolean doubleRoundRobin = request.getDoubleRoundRobin() != null && request.getDoubleRoundRobin();
            matches = generateRoundRobinMatches(round, teams, currentMatchDate, matchTimeGap, matchDuration, venue, doubleRoundRobin);
        } else if (format == FixtureFormat.DOUBLE_ROUND_ROBIN) {
            matches = generateRoundRobinMatches(round, teams, currentMatchDate, matchTimeGap, matchDuration, venue, true);
        } else {
            throw new RoundServiceException("Unsupported fixture format: " + format,
                    HttpStatus.BAD_REQUEST);
        }

        List<Match> savedMatches = matchRepository.saveAll(matches);
        log.info("Generated {} matches for round ID: {}", savedMatches.size(), roundId);

        // Convert to MatchResponse DTOs
        return savedMatches.stream()
                .map(this::convertToMatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Generate single elimination bracket matches - FIRST ROUND ONLY
     * Generates only the initial round of matches. Subsequent rounds should be generated
     * after winners are determined from previous matches.
     * For 4 teams: generates 2 matches (semifinals)
     * For 8 teams: generates 4 matches (quarterfinals)
     */
    private List<Match> generateSingleEliminationMatches(TournamentRound round, List<Team> teams,
                                                         LocalDateTime startDate, int matchTimeGap,
                                                         int matchDuration, Venue venue) {
        List<Match> matches = new ArrayList<>();
        LocalDateTime currentDate = startDate;
        int matchOrder = 1;

        // Sort teams by seed position if available
        List<RoundTeam> roundTeams = roundTeamRepository.findByRoundId(round.getId());
        teams.sort((t1, t2) -> {
            Integer seed1 = roundTeams.stream()
                    .filter(rt -> rt.getTeam() != null && rt.getTeam().getId().equals(t1.getId()))
                    .findFirst()
                    .map(RoundTeam::getSeedPosition)
                    .orElse(null);
            Integer seed2 = roundTeams.stream()
                    .filter(rt -> rt.getTeam() != null && rt.getTeam().getId().equals(t2.getId()))
                    .findFirst()
                    .map(RoundTeam::getSeedPosition)
                    .orElse(null);
            if (seed1 == null && seed2 == null) return 0;
            if (seed1 == null) return 1;
            if (seed2 == null) return -1;
            return seed1.compareTo(seed2);
        });

int numTeams = teams.size();

        // Generate ONLY first round matches (e.g., semifinals for 4 teams, quarterfinals for 8 teams)
        // Future rounds with winners should be generated separately after matches complete
        int matchesInRound = numTeams / 2;
        String roundName = getRoundNameForBracket(numTeams);

        for (int i = 0; i < matchesInRound; i++) {
            int team1Index = i * 2;
            int team2Index = team1Index + 1;

            if (team2Index < teams.size()) {
                Team homeTeam = teams.get(team1Index);
                Team awayTeam = teams.get(team2Index);

                Match match = createRoundMatch(
                        round,
                        homeTeam,
                        awayTeam,
                        currentDate,
                        matchDuration,
                        venue,
                        matchOrder++,
                        roundName,
                        1  // Always round number 1 (first round only)
                );
                matches.add(match);
                // Next match starts after current match ends + gap
                currentDate = currentDate.plusMinutes(matchDuration).plusMinutes(matchTimeGap);
            }
        }

        return matches;
    }

    /**
     * Generate round-robin matches (all teams play each other)
     */
    private List<Match> generateRoundRobinMatches(TournamentRound round, List<Team> teams,
                                                   LocalDateTime startDate, int matchTimeGap,
                                                   int matchDuration, Venue venue, boolean doubleRoundRobin) {
        List<Match> matches = new ArrayList<>();
        LocalDateTime currentDate = startDate;
        int matchOrder = 1;

        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                Team homeTeam = teams.get(i);
                Team awayTeam = teams.get(j);

                // First match (home vs away)
                Match match1 = createRoundMatch(
                        round,
                        homeTeam,
                        awayTeam,
                        currentDate,
                        matchDuration,
                        venue,
                        matchOrder++,
                        "Round Robin",
                        1
                );
                matches.add(match1);
                // Next match starts after current match ends + gap
                currentDate = currentDate.plusMinutes(matchDuration).plusMinutes(matchTimeGap);

                // Second match (away vs home) if double round-robin
                if (doubleRoundRobin) {
                    Match match2 = createRoundMatch(
                            round,
                            awayTeam,
                            homeTeam,
                            currentDate,
                            matchDuration,
                            venue,
                            matchOrder++,
                            "Round Robin",
                            2
                    );
                    matches.add(match2);
                    // Next match starts after current match ends + gap
                    currentDate = currentDate.plusMinutes(matchDuration).plusMinutes(matchTimeGap);
                }
            }
        }

        return matches;
    }

    private String getRoundNameForBracket(int teamCount) {
        switch (teamCount) {
            case 2: return "Final";
            case 4: return "Semi-Final";
            case 8: return "Quarter-Final";
            case 16: return "Round of 16";
            case 32: return "Round of 32";
            default: return "Round " + teamCount;
        }
    }

    private Match createRoundMatch(TournamentRound round, Team homeTeam, Team awayTeam,
                                   LocalDateTime matchDate, int durationMinutes,
                                   Venue venue, int matchOrder, String bracketPosition, int seriesNumber) {
        return Match.builder()
                .tournament(round.getTournament())
                .round(round)
                .group(null) // Direct knockout rounds don't have groups
                .groupName(null) // No group for direct knockout rounds
                .legacyRound(round.getRoundNumber())  // Set legacyRound from roundNumber for backward compatibility
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .matchDate(matchDate)
                .matchStatus(MatchStatus.SCHEDULED)
                .matchDurationMinutes(durationMinutes)
                .venue(venue)
                .matchOrder(matchOrder)
                .bracketPosition(bracketPosition)
                .seriesNumber(seriesNumber)
                .homeTeamScore(0)
                .awayTeamScore(0)
                .elapsedTimeSeconds(0)
                .isPlaceholderMatch(false)
                .build();
    }

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
    public void checkAndAutoCompleteRound(Long roundId) {
        TournamentRound round = tournamentRoundRepository.findById(roundId)
                .orElse(null);
        
        if (round == null || round.getStatus() != RoundStatus.ONGOING) {
            // Only auto-complete rounds that are ONGOING
            return;
        }

        // Check if all matches are completed
        boolean allMatchesCompleted = areAllMatchesCompletedInRound(round);
        
        if (allMatchesCompleted) {
            log.info("Auto-completing round ID: {} - all matches are completed", roundId);
            
            // Recalculate standings for group-based rounds
            if (round.getRoundType() == RoundType.GROUP_BASED) {
                recalculateGroupStandings(round);
            }
            
            // Update round status to COMPLETED
            round.setStatus(RoundStatus.COMPLETED);
            tournamentRoundRepository.save(round);
            
            log.info("Round ID: {} auto-completed successfully", roundId);
        }
    }
}
