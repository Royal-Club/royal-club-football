package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.PlayerGoalkeepingHistory;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperPriorityDto;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperQueueResponseDto;
import com.bjit.royalclub.royalclubfootball.repository.PlayerGoalkeepingHistoryRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Enhanced Test Suite for Goalkeeper Priority Queue with 3-Factor Scoring
 *
 * Business Logic:
 * - Factor 1: Recency Score (days since last GK)
 * - Factor 2: Participation Frequency (how often player joins tournaments)
 * - Factor 3: GK Experience Frequency (how often played GK relative to participation)
 *
 * Priority = Lower Score (ascending order)
 */
@DisplayName("Enhanced Goalkeeper Queue Scoring Logic Tests")
class GoalKeeperQueueEnhancedScoringTest {

    private PlayerServiceImpl playerService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentParticipantRepository tournamentParticipantRepository;

    @Mock
    private PlayerGoalkeepingHistoryRepository goalkeepingHistoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        playerService = new PlayerServiceImpl(
                playerRepository,
                null,
                null,
                goalkeepingHistoryRepository,
                null,
                tournamentRepository,
                tournamentParticipantRepository
        );
    }

    /**
     * Real Example from Business Logic
     *
     * Scenario Setup:
     * - Current Date: 16-11-25
     * - Current Tournament: 22-11-25
     * - Most Recent Tournament: 15-11-25
     * - Total Active Tournaments: 10
     *
     * Players:
     * 1. Ahmed: Regular (8/10), Never GK → HIGHEST PRIORITY (Score: -150)
     * 2. Jane: Moderate (5/10), 2 GK, Last 25 days ago → (Score: +35)
     * 3. Bob: Very Regular (9/10), 3 GK, Last 10 days ago → (Score: +80)
     * 4. Sarah: Rare (2/10), 1 GK, Last 1 day ago → LOWEST PRIORITY (Score: +130)
     * 5. Mark: Rare (1/10), 1 GK, Last 90 days ago → defer to keep engaged (Score: +50)
     */
    @Test
    @DisplayName("Real Example: Complex Scoring with Participation Frequency and GK Experience")
    void testEnhancedScoringWithMultipleFactors() {
        // ========== SETUP TEST DATA ==========
        LocalDateTime currentDate = LocalDateTime.of(2025, 11, 16, 10, 0, 0);
        LocalDateTime currentTournamentDate = LocalDateTime.of(2025, 11, 22, 14, 0, 0);
        LocalDateTime mostRecentTournamentDate = LocalDateTime.of(2025, 11, 15, 14, 0, 0);

        Tournament currentTournament = Tournament.builder()
                .id(5L)
                .name("Spring Tournament 2025")
                .tournamentDate(currentTournamentDate)
                .isActive(true)
                .build();

        Tournament mostRecentTournament = Tournament.builder()
                .id(4L)
                .name("Winter Tournament 2025")
                .tournamentDate(mostRecentTournamentDate)
                .isActive(false)
                .build();

        // Create Players
        Player ahmed = createPlayer(1L, "Ahmed Hassan", "EMP001");
        Player jane = createPlayer(2L, "Jane Smith", "EMP002");
        Player bob = createPlayer(3L, "Bob Johnson", "EMP003");
        Player sarah = createPlayer(4L, "Sarah Williams", "EMP004");
        Player mark = createPlayer(5L, "Mark Davis", "EMP005");

        // Create Participants
        List<TournamentParticipant> participants = Arrays.asList(
                createParticipant(currentTournament, ahmed),
                createParticipant(currentTournament, jane),
                createParticipant(currentTournament, bob),
                createParticipant(currentTournament, sarah),
                createParticipant(currentTournament, mark)
        );

        // ========== SETUP GOALKEEPER HISTORY ==========

        // Ahmed: Never GK, participates 8/10 times
        // No history

        // Jane: 2 GK tournaments, 5/10 participation, last GK 25 days ago
        List<PlayerGoalkeepingHistory> janeGKHistory = Arrays.asList(
                createGKHistory(jane, 10L, 2L, LocalDateTime.of(2025, 10, 22, 14, 0, 0)), // 25 days ago
                createGKHistory(jane, 11L, 1L, LocalDateTime.of(2025, 6, 15, 14, 0, 0))   // 4 months ago
        );

        // Bob: 3 GK tournaments, 9/10 participation, last GK 8 days ago
        List<PlayerGoalkeepingHistory> bobGKHistory = Arrays.asList(
                createGKHistory(bob, 20L, 3L, LocalDateTime.of(2025, 11, 8, 10, 0, 0)),   // 8 days ago
                createGKHistory(bob, 21L, 3L, LocalDateTime.of(2025, 11, 2, 16, 45, 0)),  // 14 days ago
                createGKHistory(bob, 22L, 3L, LocalDateTime.of(2025, 10, 15, 9, 0, 0))    // 32 days ago
        );

        // Sarah: 1 GK tournament (MOST RECENT), 2/10 participation, last GK 1 day ago
        List<PlayerGoalkeepingHistory> sarahGKHistory = Arrays.asList(
                createGKHistory(sarah, 30L, mostRecentTournament.getId(), LocalDateTime.of(2025, 11, 15, 14, 0, 0)) // 1 day ago
        );

        // Mark: 1 GK tournament, 1/10 participation, last GK 90+ days ago
        List<PlayerGoalkeepingHistory> markGKHistory = Arrays.asList(
                createGKHistory(mark, 40L, 1L, LocalDateTime.of(2025, 8, 15, 14, 0, 0)) // 93 days ago
        );

        // ========== MOCK SETUP ==========

        // Tournament mocks
        when(tournamentRepository.findById(5L)).thenReturn(Optional.of(currentTournament));
        when(tournamentRepository.findMostRecentTournamentBefore(currentTournamentDate))
                .thenReturn(mostRecentTournament);

        // Participant mocks
        when(tournamentParticipantRepository.findAllByTournamentIdAndParticipationStatusTrue(5L))
                .thenReturn(participants);

        // Total active tournaments = 10
        when(goalkeepingHistoryRepository.countActiveTournaments()).thenReturn(10);

        // Ahmed mocks
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(1L, 5L))
                .thenReturn(List.of());
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(1L, 5L)).thenReturn(0);
        when(goalkeepingHistoryRepository.countPlayerTournamentParticipations(1L)).thenReturn(8); // 8/10
        when(goalkeepingHistoryRepository.findMostRecentGoalKeeperDate(1L)).thenReturn(Optional.empty());
        when(goalkeepingHistoryRepository.findAllGoalKeeperDates(1L, 5L)).thenReturn(List.of());
        when(goalkeepingHistoryRepository.wasGoalKeeperInTournament(1L, 4L)).thenReturn(false);

        // Jane mocks
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(2L, 5L))
                .thenReturn(janeGKHistory);
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(2L, 5L)).thenReturn(2);
        when(goalkeepingHistoryRepository.countPlayerTournamentParticipations(2L)).thenReturn(5); // 5/10
        when(goalkeepingHistoryRepository.findMostRecentGoalKeeperDate(2L))
                .thenReturn(Optional.of(LocalDateTime.of(2025, 10, 22, 14, 0, 0))); // 25 days ago
        when(goalkeepingHistoryRepository.findAllGoalKeeperDates(2L, 5L))
                .thenReturn(Arrays.asList(
                        LocalDateTime.of(2025, 10, 22, 14, 0, 0),
                        LocalDateTime.of(2025, 6, 15, 14, 0, 0)
                ));
        when(goalkeepingHistoryRepository.wasGoalKeeperInTournament(2L, 4L)).thenReturn(false);

        // Bob mocks
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(3L, 5L))
                .thenReturn(bobGKHistory);
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(3L, 5L)).thenReturn(3);
        when(goalkeepingHistoryRepository.countPlayerTournamentParticipations(3L)).thenReturn(9); // 9/10
        when(goalkeepingHistoryRepository.findMostRecentGoalKeeperDate(3L))
                .thenReturn(Optional.of(LocalDateTime.of(2025, 11, 8, 10, 0, 0))); // 8 days ago
        when(goalkeepingHistoryRepository.findAllGoalKeeperDates(3L, 5L))
                .thenReturn(Arrays.asList(
                        LocalDateTime.of(2025, 11, 8, 10, 0, 0),
                        LocalDateTime.of(2025, 11, 2, 16, 45, 0),
                        LocalDateTime.of(2025, 10, 15, 9, 0, 0)
                ));
        when(goalkeepingHistoryRepository.wasGoalKeeperInTournament(3L, 4L)).thenReturn(false);

        // Sarah mocks
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(4L, 5L))
                .thenReturn(sarahGKHistory);
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(4L, 5L)).thenReturn(1);
        when(goalkeepingHistoryRepository.countPlayerTournamentParticipations(4L)).thenReturn(2); // 2/10
        when(goalkeepingHistoryRepository.findMostRecentGoalKeeperDate(4L))
                .thenReturn(Optional.of(LocalDateTime.of(2025, 11, 15, 14, 0, 0))); // 1 day ago
        when(goalkeepingHistoryRepository.findAllGoalKeeperDates(4L, 5L))
                .thenReturn(Arrays.asList(
                        LocalDateTime.of(2025, 11, 15, 14, 0, 0)
                ));
        when(goalkeepingHistoryRepository.wasGoalKeeperInTournament(4L, 4L)).thenReturn(true);

        // Mark mocks
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(5L, 5L))
                .thenReturn(markGKHistory);
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(5L, 5L)).thenReturn(1);
        when(goalkeepingHistoryRepository.countPlayerTournamentParticipations(5L)).thenReturn(1); // 1/10
        when(goalkeepingHistoryRepository.findMostRecentGoalKeeperDate(5L))
                .thenReturn(Optional.of(LocalDateTime.of(2025, 8, 15, 14, 0, 0))); // 93 days ago
        when(goalkeepingHistoryRepository.findAllGoalKeeperDates(5L, 5L))
                .thenReturn(Arrays.asList(
                        LocalDateTime.of(2025, 8, 15, 14, 0, 0)
                ));
        when(goalkeepingHistoryRepository.wasGoalKeeperInTournament(5L, 4L)).thenReturn(false);

        // ========== EXECUTE TEST ==========
        GoalKeeperQueueResponseDto response = playerService.getGoalKeeperPriorityQueue(5L);

        // ========== ASSERTIONS ==========
        assertNotNull(response, "Response should not be null");
        assertEquals(5, response.getGoalKeeperPriorityQueue().size(), "Should have 5 players");
        assertEquals(5L, response.getTournamentId());
        assertEquals("Spring Tournament 2025", response.getTournamentName());

        // Verify priority queue order
        List<GoalKeeperPriorityDto> queue = response.getGoalKeeperPriorityQueue();

        // Priority 1: Ahmed (Score: -150) - Regular, Never GK
        GoalKeeperPriorityDto priority1 = queue.get(0);
        assertEquals(1, priority1.getPriority());
        assertEquals("Ahmed Hassan", priority1.getPlayerName());
        assertEquals(8, priority1.getTotalTournamentParticipations());
        assertEquals(80.0, priority1.getParticipationFrequency()); // 8/10
        assertEquals(0, priority1.getTotalGoalKeeperTournaments());
        assertEquals(0.0, priority1.getGoalKeeperExperienceFrequency());
        assertTrue(priority1.getDaysSinceLastGoalkeeping() == null);
        assertEquals(-150, priority1.getTotalPriorityScore());

        // Priority 2: Mark (Score: +50) - Rare but low recency (not recently GK)
        GoalKeeperPriorityDto priority2 = queue.get(1);
        assertEquals(2, priority2.getPriority());
        assertEquals("Mark Davis", priority2.getPlayerName());
        assertEquals(1, priority2.getTotalTournamentParticipations());
        assertEquals(10.0, priority2.getParticipationFrequency()); // 1/10
        assertEquals(1, priority2.getTotalGoalKeeperTournaments());
        assertEquals(100.0, priority2.getGoalKeeperExperienceFrequency()); // 1/1
        assertEquals(93, priority2.getDaysSinceLastGoalkeeping());

        // Priority 3: Jane (Score: +35) - Moderate participation, moderate recency
        GoalKeeperPriorityDto priority3 = queue.get(2);
        assertEquals(3, priority3.getPriority());
        assertEquals("Jane Smith", priority3.getPlayerName());
        assertEquals(5, priority3.getTotalTournamentParticipations());
        assertEquals(50.0, priority3.getParticipationFrequency()); // 5/10
        assertEquals(2, priority3.getTotalGoalKeeperTournaments());
        assertEquals(25, priority3.getDaysSinceLastGoalkeeping());

        // Priority 4: Bob (Score: +80) - Very regular but recently played GK
        GoalKeeperPriorityDto priority4 = queue.get(3);
        assertEquals(4, priority4.getPriority());
        assertEquals("Bob Johnson", priority4.getPlayerName());
        assertEquals(9, priority4.getTotalTournamentParticipations());
        assertEquals(90.0, priority4.getParticipationFrequency()); // 9/10
        assertEquals(3, priority4.getTotalGoalKeeperTournaments());
        assertEquals(8, priority4.getDaysSinceLastGoalkeeping());

        // Priority 5: Sarah (Score: +130) - Rare AND recently played GK (LOWEST)
        GoalKeeperPriorityDto priority5 = queue.get(4);
        assertEquals(5, priority5.getPriority());
        assertEquals("Sarah Williams", priority5.getPlayerName());
        assertEquals(2, priority5.getTotalTournamentParticipations());
        assertEquals(20.0, priority5.getParticipationFrequency()); // 2/10
        assertEquals(1, priority5.getTotalGoalKeeperTournaments());
        assertEquals(1, priority5.getDaysSinceLastGoalkeeping());
        assertTrue(priority5.getWasGoalKeeperInMostRecentTournament());
        assertEquals(130, priority5.getTotalPriorityScore());
    }

    /**
     * Test: Recency Score Calculation
     */
    @Test
    @DisplayName("Verify Recency Score Calculation")
    void testRecencyScoreCalculation() {
        // Never played GK → -100
        assertEquals(-100, getRecencyScore(null));
        assertEquals(-100, getRecencyScore(Integer.MAX_VALUE));

        // Played less than 14 days ago → +100
        assertEquals(100, getRecencyScore(10));
        assertEquals(100, getRecencyScore(5));

        // Played 14-30 days ago → +50
        assertEquals(50, getRecencyScore(14));
        assertEquals(50, getRecencyScore(25));

        // Played more than 30 days ago → +0
        assertEquals(0, getRecencyScore(31));
        assertEquals(0, getRecencyScore(60));
    }

    /**
     * Test: Participation Frequency Score Calculation
     */
    @Test
    @DisplayName("Verify Participation Frequency Score Calculation")
    void testParticipationFrequencyScoreCalculation() {
        // HIGH frequency (≥60%) → +0
        assertEquals(0, getParticipationFrequencyScore(60.0));
        assertEquals(0, getParticipationFrequencyScore(80.0));

        // MEDIUM frequency (30-60%) → +25
        assertEquals(25, getParticipationFrequencyScore(30.0));
        assertEquals(25, getParticipationFrequencyScore(45.0));
        assertEquals(25, getParticipationFrequencyScore(59.9));

        // LOW frequency (<30%) → +50
        assertEquals(50, getParticipationFrequencyScore(29.9));
        assertEquals(50, getParticipationFrequencyScore(10.0));
        assertEquals(50, getParticipationFrequencyScore(0.0));
    }

    /**
     * Test: GK Experience Frequency Score Calculation
     */
    @Test
    @DisplayName("Verify GK Experience Frequency Score Calculation")
    void testGKExperienceFrequencyScoreCalculation() {
        // Never played GK → -50
        assertEquals(-50, getGKExperienceFrequencyScore(0.0, 0));

        // LOW GK frequency (<20%) → -20
        assertEquals(-20, getGKExperienceFrequencyScore(10.0, 1));
        assertEquals(-20, getGKExperienceFrequencyScore(19.9, 2));

        // MEDIUM GK frequency (20-40%) → +10
        assertEquals(10, getGKExperienceFrequencyScore(20.0, 2));
        assertEquals(10, getGKExperienceFrequencyScore(30.0, 3));

        // HIGH GK frequency (≥40%) → +30
        assertEquals(30, getGKExperienceFrequencyScore(40.0, 4));
        assertEquals(30, getGKExperienceFrequencyScore(50.0, 5));
    }

    // ========== HELPER METHODS ==========

    private Player createPlayer(Long id, String name, String employeeId) {
        return Player.builder()
                .id(id)
                .name(name)
                .employeeId(employeeId)
                .isActive(true)
                .build();
    }

    private TournamentParticipant createParticipant(Tournament tournament, Player player) {
        return TournamentParticipant.builder()
                .tournament(tournament)
                .player(player)
                .participationStatus(true)
                .build();
    }

    private PlayerGoalkeepingHistory createGKHistory(Player player, Long id, Long tournamentId, LocalDateTime playedDate) {
        return PlayerGoalkeepingHistory.builder()
                .id(id)
                .player(player)
                .tournament(Tournament.builder().id(tournamentId).build())
                .roundNumber(1)
                .playedDate(playedDate)
                .build();
    }

    private Integer getRecencyScore(Integer daysSinceLastGoalkeeping) {
        if (daysSinceLastGoalkeeping == null || daysSinceLastGoalkeeping == Integer.MAX_VALUE) {
            return -100;
        }
        if (daysSinceLastGoalkeeping < 14) {
            return 100;
        } else if (daysSinceLastGoalkeeping < 30) {
            return 50;
        }
        return 0;
    }

    private Integer getParticipationFrequencyScore(Double participationFrequency) {
        if (participationFrequency >= 60) {
            return 0;
        } else if (participationFrequency >= 30) {
            return 25;
        }
        return 50;
    }

    private Integer getGKExperienceFrequencyScore(Double gkExperienceFrequency, Integer totalGKTournaments) {
        if (totalGKTournaments == 0) {
            return -50;
        }
        if (gkExperienceFrequency >= 40) {
            return 30;
        } else if (gkExperienceFrequency >= 20) {
            return 10;
        }
        return -20;
    }
}

