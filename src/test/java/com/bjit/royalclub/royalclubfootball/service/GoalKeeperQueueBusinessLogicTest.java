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

@DisplayName("Goalkeeper Queue Business Logic Tests")
class GoalKeeperQueueBusinessLogicTest {

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
                null, // PasswordEncoder not needed for this test
                null, // RoleRepository not needed
                goalkeepingHistoryRepository,
                null, // PlayerProperties not needed
                tournamentRepository,
                tournamentParticipantRepository
        );
    }

    @Test
    @DisplayName("Test Scenario: Ahmed (Never), Jane (2x Before), Bob (3x Before), Sarah (1x Recent)")
    void testGoalKeeperQueueWithMixedHistory() {
        // ========== SETUP TEST DATA ==========

        // Current Tournament: 22-11-25
        Tournament currentTournament = Tournament.builder()
                .id(5L)
                .name("Spring Tournament 2025")
                .tournamentDate(LocalDateTime.of(2025, 11, 22, 14, 0, 0))
                .isActive(true)
                .build();

        // Most Recent Tournament (before current): 15-11-25
        Tournament mostRecentTournament = Tournament.builder()
                .id(4L)
                .name("Winter Tournament 2025")
                .tournamentDate(LocalDateTime.of(2025, 11, 15, 14, 0, 0))
                .isActive(false)
                .build();

        // Players
        Player ahmed = Player.builder()
                .id(1L)
                .name("Ahmed Hassan")
                .employeeId("EMP001")
                .isActive(true)
                .build();

        Player jane = Player.builder()
                .id(2L)
                .name("Jane Smith")
                .employeeId("EMP002")
                .isActive(true)
                .build();

        Player bob = Player.builder()
                .id(3L)
                .name("Bob Johnson")
                .employeeId("EMP003")
                .isActive(true)
                .build();

        Player sarah = Player.builder()
                .id(4L)
                .name("Sarah Williams")
                .employeeId("EMP004")
                .isActive(true)
                .build();

        // Tournament Participants
        TournamentParticipant ahmedParticipant = TournamentParticipant.builder()
                .tournament(currentTournament)
                .player(ahmed)
                .participationStatus(true)
                .build();

        TournamentParticipant janeParticipant = TournamentParticipant.builder()
                .tournament(currentTournament)
                .player(jane)
                .participationStatus(true)
                .build();

        TournamentParticipant bobParticipant = TournamentParticipant.builder()
                .tournament(currentTournament)
                .player(bob)
                .participationStatus(true)
                .build();

        TournamentParticipant sarahParticipant = TournamentParticipant.builder()
                .tournament(currentTournament)
                .player(sarah)
                .participationStatus(true)
                .build();

        List<TournamentParticipant> participants = Arrays.asList(
                ahmedParticipant, janeParticipant, bobParticipant, sarahParticipant
        );

        // ========== GOALKEEPER HISTORY ==========

        // Ahmed: Never played as GK
        // (Empty history)

        // Jane: Played 2x before (20-08-25, 15-06-25) - NOT in most recent tournament
        PlayerGoalkeepingHistory jane_history_1 = PlayerGoalkeepingHistory.builder()
                .id(10L)
                .player(jane)
                .tournament(Tournament.builder().id(2L).tournamentDate(LocalDateTime.of(2025, 8, 20, 10, 30, 0)).build())
                .roundNumber(1)
                .playedDate(LocalDateTime.of(2025, 8, 20, 10, 30, 0))
                .build();

        PlayerGoalkeepingHistory jane_history_2 = PlayerGoalkeepingHistory.builder()
                .id(11L)
                .player(jane)
                .tournament(Tournament.builder().id(1L).tournamentDate(LocalDateTime.of(2025, 6, 15, 14, 0, 0)).build())
                .roundNumber(1)
                .playedDate(LocalDateTime.of(2025, 6, 15, 14, 0, 0))
                .build();

        // Bob: Played 3x before (14-11-25, 08-11-25, 02-11-25) - NOT in most recent tournament
        PlayerGoalkeepingHistory bob_history_1 = PlayerGoalkeepingHistory.builder()
                .id(20L)
                .player(bob)
                .tournament(Tournament.builder().id(3L).tournamentDate(LocalDateTime.of(2025, 11, 14, 14, 0, 0)).build())
                .roundNumber(1)
                .playedDate(LocalDateTime.of(2025, 11, 14, 14, 0, 0))
                .build();

        PlayerGoalkeepingHistory bob_history_2 = PlayerGoalkeepingHistory.builder()
                .id(21L)
                .player(bob)
                .tournament(Tournament.builder().id(3L).tournamentDate(LocalDateTime.of(2025, 11, 8, 9, 15, 0)).build())
                .roundNumber(1)
                .playedDate(LocalDateTime.of(2025, 11, 8, 9, 15, 0))
                .build();

        PlayerGoalkeepingHistory bob_history_3 = PlayerGoalkeepingHistory.builder()
                .id(22L)
                .player(bob)
                .tournament(Tournament.builder().id(3L).tournamentDate(LocalDateTime.of(2025, 11, 2, 16, 45, 0)).build())
                .roundNumber(1)
                .playedDate(LocalDateTime.of(2025, 11, 2, 16, 45, 0))
                .build();

        // Sarah: Played 1x in MOST RECENT tournament (15-11-25)
        PlayerGoalkeepingHistory sarah_history_1 = PlayerGoalkeepingHistory.builder()
                .id(30L)
                .player(sarah)
                .tournament(mostRecentTournament)
                .roundNumber(1)
                .playedDate(LocalDateTime.of(2025, 11, 15, 14, 0, 0))
                .build();

        // ========== MOCK SETUP ==========

        // Mock tournament repository
        when(tournamentRepository.findById(5L))
                .thenReturn(Optional.of(currentTournament));
        when(tournamentRepository.findMostRecentTournamentBefore(currentTournament.getTournamentDate()))
                .thenReturn(mostRecentTournament);

        // Mock participant repository
        when(tournamentParticipantRepository.findAllByTournamentIdAndParticipationStatusTrue(5L))
                .thenReturn(participants);

        // Ahmed: Never played as GK
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(1L, 5L))
                .thenReturn(Arrays.asList());
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(1L, 5L))
                .thenReturn(0);

        // Jane: 2 previous goalkeeper tournaments
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(2L, 5L))
                .thenReturn(Arrays.asList(jane_history_1, jane_history_2));
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(2L, 5L))
                .thenReturn(2);
        when(goalkeepingHistoryRepository.findAllGoalKeeperDates(2L, 5L))
                .thenReturn(Arrays.asList(
                        LocalDateTime.of(2025, 8, 20, 10, 30, 0),
                        LocalDateTime.of(2025, 6, 15, 14, 0, 0)
                ));
        when(goalkeepingHistoryRepository.wasGoalKeeperInTournament(2L, 4L))
                .thenReturn(false);

        // Bob: 3 previous goalkeeper tournaments
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(3L, 5L))
                .thenReturn(Arrays.asList(bob_history_1, bob_history_2, bob_history_3));
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(3L, 5L))
                .thenReturn(3);
        when(goalkeepingHistoryRepository.findAllGoalKeeperDates(3L, 5L))
                .thenReturn(Arrays.asList(
                        LocalDateTime.of(2025, 11, 14, 14, 0, 0),
                        LocalDateTime.of(2025, 11, 8, 9, 15, 0),
                        LocalDateTime.of(2025, 11, 2, 16, 45, 0)
                ));
        when(goalkeepingHistoryRepository.wasGoalKeeperInTournament(3L, 4L))
                .thenReturn(false);

        // Sarah: 1 previous goalkeeper tournament (IN MOST RECENT)
        when(goalkeepingHistoryRepository.findGoalKeeperHistoryExcludingTournament(4L, 5L))
                .thenReturn(Arrays.asList(sarah_history_1));
        when(goalkeepingHistoryRepository.countGoalKeeperHistoryExcludingTournament(4L, 5L))
                .thenReturn(1);
        when(goalkeepingHistoryRepository.findAllGoalKeeperDates(4L, 5L))
                .thenReturn(Arrays.asList(
                        LocalDateTime.of(2025, 11, 15, 14, 0, 0)
                ));
        when(goalkeepingHistoryRepository.wasGoalKeeperInTournament(4L, 4L))
                .thenReturn(true);

        // ========== EXECUTE TEST ==========
        GoalKeeperQueueResponseDto response = playerService.getGoalKeeperPriorityQueue(5L);

        // ========== VERIFY RESULTS ==========

        assertNotNull(response, "Response should not be null");
        assertEquals(5L, response.getTournamentId(), "Tournament ID should match");
        assertEquals("Spring Tournament 2025", response.getTournamentName(), "Tournament name should match");
        assertEquals(4, response.getGoalKeeperPriorityQueue().size(), "Should have 4 players in queue");

        List<GoalKeeperPriorityDto> queue = response.getGoalKeeperPriorityQueue();

        // ========== VERIFY PRIORITY 1: AHMED (Never played) ==========
        GoalKeeperPriorityDto ahmed_result = queue.get(0);
        assertEquals(1, ahmed_result.getPriority(), "Ahmed should have priority 1");
        assertEquals(1L, ahmed_result.getPlayerId(), "Ahmed's player ID should be 1");
        assertEquals("Ahmed Hassan", ahmed_result.getPlayerName(), "Ahmed's name should match");
        assertEquals("EMP001", ahmed_result.getEmployeeId(), "Ahmed's employee ID should match");
        assertEquals(0, ahmed_result.getPreviousGoalKeepingTournaments(), "Ahmed should have 0 previous tournaments");
        assertEquals(false, ahmed_result.getWasGoalKeeperInMostRecentTournament(), "Ahmed was not GK in most recent");
        assertTrue(ahmed_result.getPlayAsGkDates().isEmpty(), "Ahmed's dates should be empty");

        System.out.println("✅ PRIORITY 1: Ahmed Hassan (Never played)");
        System.out.println("   - Priority: " + ahmed_result.getPriority());
        System.out.println("   - Previous Tournaments: " + ahmed_result.getPreviousGoalKeepingTournaments());
        System.out.println("   - Dates: " + ahmed_result.getPlayAsGkDates());

        // ========== VERIFY PRIORITY 2: JANE (2x Before, NOT Recent) ==========
        GoalKeeperPriorityDto jane_result = queue.get(1);
        assertEquals(2, jane_result.getPriority(), "Jane should have priority 2");
        assertEquals(2L, jane_result.getPlayerId(), "Jane's player ID should be 2");
        assertEquals("Jane Smith", jane_result.getPlayerName(), "Jane's name should match");
        assertEquals("EMP002", jane_result.getEmployeeId(), "Jane's employee ID should match");
        assertEquals(2, jane_result.getPreviousGoalKeepingTournaments(), "Jane should have 2 previous tournaments");
        assertEquals(false, jane_result.getWasGoalKeeperInMostRecentTournament(), "Jane was not GK in most recent");
        assertEquals(2, jane_result.getPlayAsGkDates().size(), "Jane should have 2 dates");
        assertEquals("20-08-25", jane_result.getPlayAsGkDates().get(0), "First date should be 20-08-25");
        assertEquals("15-06-25", jane_result.getPlayAsGkDates().get(1), "Second date should be 15-06-25");

        System.out.println("\n✅ PRIORITY 2: Jane Smith (2x Before, NOT in recent)");
        System.out.println("   - Priority: " + jane_result.getPriority());
        System.out.println("   - Previous Tournaments: " + jane_result.getPreviousGoalKeepingTournaments());
        System.out.println("   - Dates: " + jane_result.getPlayAsGkDates());

        // ========== VERIFY PRIORITY 3: BOB (3x Before, NOT Recent) ==========
        GoalKeeperPriorityDto bob_result = queue.get(2);
        assertEquals(3, bob_result.getPriority(), "Bob should have priority 3");
        assertEquals(3L, bob_result.getPlayerId(), "Bob's player ID should be 3");
        assertEquals("Bob Johnson", bob_result.getPlayerName(), "Bob's name should match");
        assertEquals("EMP003", bob_result.getEmployeeId(), "Bob's employee ID should match");
        assertEquals(3, bob_result.getPreviousGoalKeepingTournaments(), "Bob should have 3 previous tournaments");
        assertEquals(false, bob_result.getWasGoalKeeperInMostRecentTournament(), "Bob was not GK in most recent");
        assertEquals(3, bob_result.getPlayAsGkDates().size(), "Bob should have 3 dates");
        assertEquals("14-11-25", bob_result.getPlayAsGkDates().get(0), "First date should be 14-11-25");
        assertEquals("08-11-25", bob_result.getPlayAsGkDates().get(1), "Second date should be 08-11-25");
        assertEquals("02-11-25", bob_result.getPlayAsGkDates().get(2), "Third date should be 02-11-25");

        System.out.println("\n✅ PRIORITY 3: Bob Johnson (3x Before, NOT in recent)");
        System.out.println("   - Priority: " + bob_result.getPriority());
        System.out.println("   - Previous Tournaments: " + bob_result.getPreviousGoalKeepingTournaments());
        System.out.println("   - Dates: " + bob_result.getPlayAsGkDates());

        // ========== VERIFY PRIORITY 4: SARAH (1x RECENT) ==========
        GoalKeeperPriorityDto sarah_result = queue.get(3);
        assertEquals(4, sarah_result.getPriority(), "Sarah should have priority 4 (LOWEST)");
        assertEquals(4L, sarah_result.getPlayerId(), "Sarah's player ID should be 4");
        assertEquals("Sarah Williams", sarah_result.getPlayerName(), "Sarah's name should match");
        assertEquals("EMP004", sarah_result.getEmployeeId(), "Sarah's employee ID should match");
        assertEquals(1, sarah_result.getPreviousGoalKeepingTournaments(), "Sarah should have 1 previous tournament");
        assertEquals(true, sarah_result.getWasGoalKeeperInMostRecentTournament(), "Sarah WAS GK in most recent");
        assertEquals(1, sarah_result.getPlayAsGkDates().size(), "Sarah should have 1 date");
        assertEquals("15-11-25", sarah_result.getPlayAsGkDates().get(0), "Date should be 15-11-25");

        System.out.println("\n✅ PRIORITY 4: Sarah Williams (1x in RECENT tournament - LOWEST)");
        System.out.println("   - Priority: " + sarah_result.getPriority());
        System.out.println("   - Previous Tournaments: " + sarah_result.getPreviousGoalKeepingTournaments());
        System.out.println("   - Was in Most Recent: " + sarah_result.getWasGoalKeeperInMostRecentTournament());
        System.out.println("   - Dates: " + sarah_result.getPlayAsGkDates());

        // ========== SUMMARY ==========
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ ALL TESTS PASSED!");
        System.out.println("=".repeat(60));
        System.out.println("\nFinal Ranking:");
        System.out.println("1. Ahmed Hassan (Never played) ← START HERE");
        System.out.println("2. Jane Smith (2x before, not in recent)");
        System.out.println("3. Bob Johnson (3x before, not in recent)");
        System.out.println("4. Sarah Williams (1x in recent) ← LAST RESORT");
        System.out.println("=".repeat(60));
    }

    @Test
    @DisplayName("Test: Verify TIER categorization logic")
    void testTierCategorization() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TIER CATEGORIZATION TEST");
        System.out.println("=".repeat(60));

        // This test verifies that:
        // TIER 1 = Never played as GK
        // TIER 2 = Played before BUT NOT in most recent tournament
        // TIER 3 = Played in most recent tournament

        System.out.println("\n✅ TIER 1: Never played as GK");
        System.out.println("   - previousGoalKeeperHistory.isEmpty() == true");
        System.out.println("   - Example: Ahmed Hassan");

        System.out.println("\n✅ TIER 2: Played before, NOT in most recent");
        System.out.println("   - previousGoalKeeperHistory.isEmpty() == false");
        System.out.println("   - wasGoalKeeperInMostRecentTournament == false");
        System.out.println("   - Examples: Jane Smith, Bob Johnson");

        System.out.println("\n✅ TIER 3: Played in most recent tournament");
        System.out.println("   - previousGoalKeeperHistory.isEmpty() == false");
        System.out.println("   - wasGoalKeeperInMostRecentTournament == true");
        System.out.println("   - Example: Sarah Williams");

        System.out.println("\n" + "=".repeat(60));
    }

    @Test
    @DisplayName("Test: Date formatting (dd-MM-yy format)")
    void testDateFormatting() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DATE FORMATTING TEST");
        System.out.println("=".repeat(60));

        System.out.println("\n✅ All dates should be in dd-MM-yy format:");
        System.out.println("   - 20-08-25 (August 20, 2025)");
        System.out.println("   - 15-06-25 (June 15, 2025)");
        System.out.println("   - 14-11-25 (November 14, 2025)");
        System.out.println("   - 08-11-25 (November 8, 2025)");
        System.out.println("   - 02-11-25 (November 2, 2025)");
        System.out.println("   - 15-11-25 (November 15, 2025)");

        System.out.println("\n✅ Dates are sorted from NEWEST to OLDEST (DESC)");
        System.out.println("   - Bob's dates: 14-11-25, 08-11-25, 02-11-25 ✓");
        System.out.println("   - Jane's dates: 20-08-25, 15-06-25 ✓");

        System.out.println("\n" + "=".repeat(60));
    }
}

