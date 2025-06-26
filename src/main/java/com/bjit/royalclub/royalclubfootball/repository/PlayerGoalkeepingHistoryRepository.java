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
            "LEFT JOIN PlayerGoalkeepingHistory pgh ON p.id = pgh.player.id")
    List<GoalKeeperHistoryDto> getGoalKeeperHistory();

    List<PlayerGoalkeepingHistory> getAllByPlayerIdOrderByRoundNumberDesc(Long playerId);

}
