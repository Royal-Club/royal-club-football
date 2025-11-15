package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.PlayerGoalkeepingHistory;
import com.bjit.royalclub.royalclubfootball.model.GoalKeeperHistoryDto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            "WHERE pgh.player.id = :tournamentId " +
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

}
