package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.config.GoalKeeperQueueProperties;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.PlayerGoalkeepingHistory;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperPriorityDto;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperQueueResponseDto;
import com.bjit.royalclub.royalclubfootball.repository.PlayerGoalkeepingHistoryRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the ordering rules of the goalkeeper priority queue, all of which fail quietly if they
 * regress: the queue still returns a plausible-looking ranking, it is just an unfair one, and the
 * only people who notice are the players who get asked to keep goal too often.
 * <p>
 * The fixture is five players against five past tournaments, each of which needed two keepers from
 * a turnout of ten - so one appearance accrues 0.2 of a turn. That is enough to separate the two
 * cases a raw count of turns cannot tell apart: someone whose count is low because they have been
 * skipped, and someone whose count is low because they have barely been here.
 */
class GoalKeeperPriorityQueueTest {

    private static final Long CURRENT_TOURNAMENT = 6L;
    private static final LocalDateTime CURRENT_DATE = LocalDateTime.of(2026, 8, 18, 18, 0);

    /** Five appearances, never kept goal: owed 1.0 and the only player genuinely due. */
    private static final Long VETERAN = 100L;
    /** Away for most of the season. Low count, but low attendance to match - owed nothing. */
    private static final Long RETURNER = 101L;
    /** First season. Has never kept goal, which must not be mistaken for being owed a turn. */
    private static final Long NEWCOMER = 102L;
    /** Owed as much as the veteran, but kept goal in the tournament just gone. */
    private static final Long RECENT_KEEPER = 103L;
    /** Owed the most of anyone, and opted out - eligibility outranks the ledger. */
    private static final Long OPTED_OUT = 104L;

    private PlayerGoalkeepingHistoryRepository goalkeepingHistoryRepository;
    private TournamentRepository tournamentRepository;
    private TournamentParticipantRepository tournamentParticipantRepository;
    private PlayerServiceImpl service;

    @BeforeEach
    void setUp() {
        goalkeepingHistoryRepository = mock(PlayerGoalkeepingHistoryRepository.class);
        tournamentRepository = mock(TournamentRepository.class);
        tournamentParticipantRepository = mock(TournamentParticipantRepository.class);

        GoalKeeperQueueProperties properties = new GoalKeeperQueueProperties();
        properties.setCooldownTournaments(2);
        properties.setDefaultSlotsPerTournament(2);
        properties.setEstimateMissingSlots(true);

        service = new PlayerServiceImpl(null, null, null, goalkeepingHistoryRepository, null,
                properties, tournamentRepository, tournamentParticipantRepository, null, null, null);

        Tournament current = Tournament.builder()
                .id(CURRENT_TOURNAMENT).name("Week 6").tournamentDate(CURRENT_DATE).build();
        when(tournamentRepository.findById(CURRENT_TOURNAMENT)).thenReturn(Optional.of(current));
        when(tournamentParticipantRepository.findAllByTournamentIdAndParticipationStatusTrueWithPlayer(
                CURRENT_TOURNAMENT)).thenReturn(List.of(
                participant(current, player(VETERAN, "Veteran", true)),
                participant(current, player(RETURNER, "Returner", true)),
                participant(current, player(NEWCOMER, "Newcomer", true)),
                participant(current, player(RECENT_KEEPER, "Recent Keeper", true)),
                participant(current, player(OPTED_OUT, "Opted Out", false))));

        when(goalkeepingHistoryRepository.countActiveTournaments()).thenReturn(5);

        // Who turned up to what. Obligation accrues per appearance, so this drives everything.
        List<Object[]> attendance = new ArrayList<>();
        for (long tournamentId = 1; tournamentId <= 5; tournamentId++) {
            attendance.add(new Object[]{VETERAN, tournamentId});
            attendance.add(new Object[]{RECENT_KEEPER, tournamentId});
            attendance.add(new Object[]{OPTED_OUT, tournamentId});
        }
        attendance.add(new Object[]{RETURNER, 1L});   // then away until now
        attendance.add(new Object[]{RETURNER, 5L});
        attendance.add(new Object[]{NEWCOMER, 5L});   // first season
        when(tournamentParticipantRepository.findAttendedTournamentIdsBatch(any(), anyLong()))
                .thenReturn(attendance);

        // Two keepers needed from a turnout of ten, every time: 0.2 of a turn per appearance.
        List<Object[]> slots = new ArrayList<>();
        List<Object[]> headCounts = new ArrayList<>();
        for (long tournamentId = 1; tournamentId <= 5; tournamentId++) {
            slots.add(new Object[]{tournamentId, 2L});
            headCounts.add(new Object[]{tournamentId, 10L});
        }
        when(goalkeepingHistoryRepository.countGoalKeeperSlotsByTournamentIds(any())).thenReturn(slots);
        when(tournamentParticipantRepository.countParticipantsByTournamentIds(any())).thenReturn(headCounts);

        when(goalkeepingHistoryRepository.countGoalKeeperStintsBatch(any(), anyLong(), any()))
                .thenReturn(List.of(
                        new Object[]{RETURNER, 1L},        // one turn, long ago
                        new Object[]{RECENT_KEEPER, 1L})); // one turn, last time out

        // Cooldown window: the two tournaments immediately before this one.
        when(tournamentRepository.findRecentTournamentIdsBefore(any(), any()))
                .thenReturn(List.of(5L, 4L));
        when(goalkeepingHistoryRepository.findGoalKeeperAssignmentsInTournaments(any(), any()))
                // Type witness: a lone Object[] would otherwise be spread by List.of's varargs.
                .thenReturn(List.<Object[]>of(new Object[]{RECENT_KEEPER, 5L}));

        when(goalkeepingHistoryRepository.findGoalKeeperHistoryByPlayerIdsExcludingTournament(
                any(), anyLong())).thenReturn(List.of(
                goalkeepingRow(player(RETURNER, "Returner", true), 1L,
                        LocalDateTime.of(2026, 1, 10, 18, 0)),
                goalkeepingRow(player(RECENT_KEEPER, "Recent Keeper", true), 5L,
                        LocalDateTime.of(2026, 8, 11, 18, 0))));

        when(goalkeepingHistoryRepository.countPlayerTournamentParticipationsBatch(any()))
                .thenReturn(List.of(
                        new Object[]{VETERAN, 6L}, new Object[]{RETURNER, 3L},
                        new Object[]{NEWCOMER, 2L}, new Object[]{RECENT_KEEPER, 6L},
                        new Object[]{OPTED_OUT, 6L}));
        when(tournamentParticipantRepository.findMostRecentParticipationDateBatch(any(), anyLong()))
                .thenReturn(List.of());
    }

    @Test
    void ranksByGoalkeepingOwedRatherThanTurnsTaken() {
        List<GoalKeeperPriorityDto> queue = queue();

        // Veteran 1.0 owed, newcomer 0.2, returner -0.6 - then the two held out of the ranking.
        assertThat(queue).extracting(GoalKeeperPriorityDto::getPlayerId)
                .containsExactly(VETERAN, NEWCOMER, RETURNER, RECENT_KEEPER, OPTED_OUT);
    }

    @Test
    void neverHavingKeptGoalDoesNotByItselfPutANewcomerFirst() {
        Map<Long, GoalKeeperPriorityDto> byPlayer = byPlayer();

        // Both have never kept goal. The veteran has earned the turn by turning up; the newcomer
        // has not been here long enough to owe one, which a raw count of turns cannot express.
        assertThat(byPlayer.get(NEWCOMER).getGoalKeeperStints()).isZero();
        assertThat(byPlayer.get(VETERAN).getGoalKeeperStints()).isZero();
        assertThat(byPlayer.get(NEWCOMER).getGoalKeeperDebt())
                .isLessThan(byPlayer.get(VETERAN).getGoalKeeperDebt());
        assertThat(byPlayer.get(NEWCOMER).getPriority()).isGreaterThan(byPlayer.get(VETERAN).getPriority());
    }

    @Test
    void absenceNeitherBuildsUpNorWipesOutWhatSomeoneOwes() {
        GoalKeeperPriorityDto returner = byPlayer().get(RETURNER);

        // Two appearances accrue 0.4; one turn served leaves them 0.6 ahead. The seasons they
        // missed are simply absent from the ledger - they neither owe for them nor gain from them.
        assertThat(returner.getAttendedTournaments()).isEqualTo(2);
        assertThat(returner.getAccruedObligation()).isEqualTo(0.4);
        assertThat(returner.getGoalKeeperDebt()).isEqualTo(-0.6);
    }

    @Test
    void keepingGoalRecentlyOutranksBeingOwedATurn() {
        GoalKeeperPriorityDto recentKeeper = byPlayer().get(RECENT_KEEPER);
        GoalKeeperPriorityDto returner = byPlayer().get(RETURNER);

        // Owed more than the returner, and still placed below them: the rest period is applied
        // before the ledger sorts, so no amount of debt buys a second turn in a row.
        assertThat(recentKeeper.getGoalKeeperDebt()).isGreaterThan(returner.getGoalKeeperDebt());
        assertThat(recentKeeper.getPriority()).isGreaterThan(returner.getPriority());
        assertThat(recentKeeper.getCategory()).isEqualTo("COOLDOWN");
        assertThat(recentKeeper.getCooldownRemaining()).isEqualTo(2);
    }

    @Test
    void optingOutKeepsAPlayerVisibleButUnranked() {
        GoalKeeperPriorityDto optedOut = byPlayer().get(OPTED_OUT);

        assertThat(optedOut.getCategory()).isEqualTo("EXEMPT");
        assertThat(optedOut.getPriority()).isEqualTo(5);
        // Owed as much as the veteran, and still last: willingness is not negotiable by arithmetic.
        assertThat(optedOut.getGoalKeeperDebt()).isEqualTo(1.0);
    }

    @Test
    void reportsHowMuchOfTheLedgerIsEstimatedRatherThanRecorded() {
        GoalKeeperQueueResponseDto response = service.getGoalKeeperPriorityQueue(CURRENT_TOURNAMENT);

        // Every tournament here has its keepers on record, so nothing is guessed. The counts are
        // returned either way, because a ledger built on missing history looks just as confident.
        assertThat(response.getLedgerCoverage().getTournamentsConsidered()).isEqualTo(5);
        assertThat(response.getLedgerCoverage().getTournamentsWithRecordedKeepers()).isEqualTo(5);
        assertThat(response.getLedgerCoverage().getTournamentsEstimated()).isZero();
        assertThat(response.getCooldownTournaments()).isEqualTo(2);
    }

    @Test
    void tournamentsWithNoRecordedKeeperFallBackToTheConfiguredEstimate() {
        when(goalkeepingHistoryRepository.countGoalKeeperSlotsByTournamentIds(any()))
                .thenAnswer(invocation -> {
                    Collection<?> ids = invocation.getArgument(0);
                    // Only tournament 1 has its keepers recorded; the rest must be estimated.
                    return ids.contains(1L) ? List.<Object[]>of(new Object[]{1L, 2L}) : List.of();
                });

        GoalKeeperQueueResponseDto response = service.getGoalKeeperPriorityQueue(CURRENT_TOURNAMENT);

        assertThat(response.getLedgerCoverage().getTournamentsWithRecordedKeepers()).isEqualTo(1);
        assertThat(response.getLedgerCoverage().getTournamentsEstimated()).isEqualTo(4);
        assertThat(response.getLedgerCoverage().getEstimatingMissingSlots()).isTrue();
        // The estimate matches the real figure here, so the ranking is unchanged by the gap.
        assertThat(response.getGoalKeeperPriorityQueue())
                .extracting(GoalKeeperPriorityDto::getPlayerId)
                .containsExactly(VETERAN, NEWCOMER, RETURNER, RECENT_KEEPER, OPTED_OUT);
    }

    private List<GoalKeeperPriorityDto> queue() {
        return service.getGoalKeeperPriorityQueue(CURRENT_TOURNAMENT).getGoalKeeperPriorityQueue();
    }

    private Map<Long, GoalKeeperPriorityDto> byPlayer() {
        return queue().stream().collect(
                java.util.stream.Collectors.toMap(GoalKeeperPriorityDto::getPlayerId, dto -> dto));
    }

    private static Player player(Long id, String name, boolean gkEligible) {
        return Player.builder()
                .id(id).name(name).employeeId("E" + id).gkEligible(gkEligible).build();
    }

    private static TournamentParticipant participant(Tournament tournament, Player player) {
        return TournamentParticipant.builder()
                .tournament(tournament).player(player).participationStatus(true).build();
    }

    private static PlayerGoalkeepingHistory goalkeepingRow(Player player, Long tournamentId,
                                                           LocalDateTime playedDate) {
        return PlayerGoalkeepingHistory.builder()
                .player(player)
                .tournament(Tournament.builder().id(tournamentId).tournamentDate(playedDate).build())
                .roundNumber(1)
                .playedDate(playedDate)
                .build();
    }
}
