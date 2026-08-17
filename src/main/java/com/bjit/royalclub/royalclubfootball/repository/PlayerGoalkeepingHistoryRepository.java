package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.PlayerGoalkeepingHistory;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlayerGoalkeepingHistoryRepository extends JpaRepository<PlayerGoalkeepingHistory, Long> {
    @Query("SELECT MAX(gk.roundNumber) FROM PlayerGoalkeepingHistory gk WHERE gk.player.id = :playerId")
    Optional<Integer> findMaxRoundByPlayerId(@Param("playerId") Long playerId);

    @Transactional
    @Modifying
    @Query("DELETE FROM PlayerGoalkeepingHistory gk WHERE gk.player.id = :playerId AND gk.tournament.id = :tournamentId")
    void deleteByPlayerAndTournament(@Param("playerId") Long playerId, @Param("tournamentId") Long tournamentId);

    @Query("SELECT new com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto(p.id, p.name, pgh.roundNumber, pgh.playedDate) " +
            "FROM Player p " +
            "LEFT JOIN PlayerGoalkeepingHistory pgh ON p.id = pgh.player.id " +
            "WHERE p.isActive = true")
    List<GoalKeeperHistoryDto> getGoalKeeperHistory();

    List<PlayerGoalkeepingHistory> getAllByPlayerIdOrderByRoundNumberDesc(Long playerId);

    @Query("SELECT pgh FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id = :playerId AND pgh.tournament.id != :currentTournamentId " +
            "ORDER BY pgh.playedDate DESC")
    List<PlayerGoalkeepingHistory> findGoalKeeperHistoryExcludingTournament(
            @Param("playerId") Long playerId,
            @Param("currentTournamentId") Long currentTournamentId);

    @Query("SELECT COUNT(pgh) FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id = :playerId AND pgh.tournament.id != :currentTournamentId")
    Integer countGoalKeeperHistoryExcludingTournament(
            @Param("playerId") Long playerId,
            @Param("currentTournamentId") Long currentTournamentId);

    @Query("SELECT CASE WHEN COUNT(pgh) > 0 THEN true ELSE false END " +
            "FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id = :playerId AND pgh.tournament.id = :tournamentId")
    boolean wasGoalKeeperInTournament(
            @Param("playerId") Long playerId,
            @Param("tournamentId") Long tournamentId);

    @Query("SELECT pgh FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id = :playerId AND pgh.tournament.id = :tournamentId " +
            "ORDER BY pgh.playedDate DESC LIMIT 1")
    Optional<PlayerGoalkeepingHistory> findMostRecentGoalKeeperAssignmentInTournament(
            @Param("playerId") Long playerId,
            @Param("tournamentId") Long tournamentId);

    @Query("SELECT pgh.playedDate FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id = :playerId AND pgh.tournament.id != :currentTournamentId " +
            "ORDER BY pgh.playedDate DESC")
    List<java.time.LocalDateTime> findAllGoalKeeperDates(
            @Param("playerId") Long playerId,
            @Param("currentTournamentId") Long currentTournamentId);

    @Query("SELECT COUNT(DISTINCT tp.tournament.id) FROM TournamentParticipant tp " +
            "WHERE tp.player.id = :playerId AND tp.participationStatus = true")
    Integer countPlayerTournamentParticipations(@Param("playerId") Long playerId);

    @Query("SELECT COUNT(DISTINCT t.id) FROM Tournament t WHERE t.isActive = true")
    Integer countActiveTournaments();

    @Query("SELECT MAX(pgh.playedDate) FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id = :playerId")
    Optional<LocalDateTime> findMostRecentGoalKeeperDate(@Param("playerId") Long playerId);

    // === Batch queries for GK priority queue ===

    @Query("SELECT pgh FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id IN :playerIds AND pgh.tournament.id != :currentTournamentId " +
            "ORDER BY pgh.player.id, pgh.playedDate DESC")
    List<PlayerGoalkeepingHistory> findGoalKeeperHistoryByPlayerIdsExcludingTournament(
            @Param("playerIds") Collection<Long> playerIds,
            @Param("currentTournamentId") Long currentTournamentId);

    @Query("SELECT tp.player.id, COUNT(DISTINCT tp.tournament.id) FROM TournamentParticipant tp " +
            "WHERE tp.player.id IN :playerIds AND tp.participationStatus = true " +
            "GROUP BY tp.player.id")
    List<Object[]> countPlayerTournamentParticipationsBatch(@Param("playerIds") Collection<Long> playerIds);

    @Query("SELECT pgh.player.id, MAX(pgh.playedDate) FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id IN :playerIds " +
            "GROUP BY pgh.player.id")
    List<Object[]> findMostRecentGoalKeeperDateBatch(@Param("playerIds") Collection<Long> playerIds);

    @Query("SELECT pgh.player.id FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id IN :playerIds AND pgh.tournament.id = :tournamentId")
    List<Long> findPlayerIdsWhoWereGoalKeeperInTournament(
            @Param("playerIds") Collection<Long> playerIds,
            @Param("tournamentId") Long tournamentId);

    // === Goalkeeping ledger ===

    /**
     * Keeper slots actually filled per tournament, as {tournamentId, count}. This is the numerator
     * of the obligation each attendee accrues: the work a tournament created, shared between the
     * people who turned up for it.
     * <p>
     * Counts rows, not distinct players, so a tournament that needed two keepers created twice the
     * obligation of one that needed a single keeper. Tournaments with no recorded keeper produce no
     * row - the caller decides whether that means "no obligation" or "not recorded".
     */
    @Query("SELECT pgh.tournament.id, COUNT(pgh) FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.tournament.id IN :tournamentIds " +
            "GROUP BY pgh.tournament.id")
    List<Object[]> countGoalKeeperSlotsByTournamentIds(@Param("tournamentIds") Collection<Long> tournamentIds);

    /**
     * Turns each player has actually served before the given date, as {playerId, count}.
     * <p>
     * Counts rows rather than distinct tournaments: someone who took two keeper shifts in a day paid
     * twice. Not MAX(roundNumber) either - that is a per-player lifetime ordinal which never rewinds
     * when history is deleted, so it drifts above the real total.
     * <p>
     * Bounded by date rather than only excluding the current tournament, because keeper rows are
     * written when a team sheet is filled in: a tournament further out with teams already picked
     * would otherwise be counted as a turn someone has served.
     */
    @Query("SELECT pgh.player.id, COUNT(pgh) FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id IN :playerIds AND pgh.tournament.id != :currentTournamentId " +
            "AND pgh.playedDate < :currentTournamentDate " +
            "GROUP BY pgh.player.id")
    List<Object[]> countGoalKeeperStintsBatch(
            @Param("playerIds") Collection<Long> playerIds,
            @Param("currentTournamentId") Long currentTournamentId,
            @Param("currentTournamentDate") LocalDateTime currentTournamentDate);

    /**
     * Which of the given players kept goal in any of the given tournaments, as {playerId,
     * tournamentId}. Feeds the cooldown tier, which needs to know not just whether someone is
     * resting but how far back their turn was.
     */
    @Query("SELECT DISTINCT pgh.player.id, pgh.tournament.id FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id IN :playerIds AND pgh.tournament.id IN :tournamentIds")
    List<Object[]> findGoalKeeperAssignmentsInTournaments(
            @Param("playerIds") Collection<Long> playerIds,
            @Param("tournamentIds") Collection<Long> tournamentIds);

    /** Guards against a second history row for a player who is already recorded as keeper here. */
    @Query("SELECT COUNT(pgh) FROM PlayerGoalkeepingHistory pgh " +
            "WHERE pgh.player.id = :playerId AND pgh.tournament.id = :tournamentId")
    int countByPlayerAndTournament(
            @Param("playerId") Long playerId,
            @Param("tournamentId") Long tournamentId);

}
