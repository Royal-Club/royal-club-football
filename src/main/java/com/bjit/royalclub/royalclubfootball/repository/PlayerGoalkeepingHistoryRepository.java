package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.PlayerGoalkeepingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlayerGoalkeepingHistoryRepository extends JpaRepository<PlayerGoalkeepingHistory, Long> {
    @Query("SELECT MAX(gk.roundNumber) FROM PlayerGoalkeepingHistory gk WHERE gk.player.id = :playerId")
    Optional<Integer> findMaxRoundByPlayerId(@Param("playerId") Long playerId);


}
