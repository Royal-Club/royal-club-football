package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MonthlyDuesReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface MonthlyDuesReminderLogRepository extends JpaRepository<MonthlyDuesReminderLog, Long> {

    /**
     * How many dues reminders have already been sent to this player for this month (across all channels).
     * Used to enforce the per-player, per-month reminder cap.
     */
    int countByPlayerIdAndMonthOfPayment(Long playerId, LocalDate monthOfPayment);

    /**
     * Batch: count reminders per player for a month.
     * Returns Object[] of {playerId, count}.
     */
    @Query("SELECT r.player.id, COUNT(r) FROM MonthlyDuesReminderLog r " +
            "WHERE r.monthOfPayment = :monthOfPayment AND r.player.id IN :playerIds " +
            "GROUP BY r.player.id")
    List<Object[]> countByPlayerIdsAndMonthOfPayment(
            @Param("playerIds") Collection<Long> playerIds,
            @Param("monthOfPayment") LocalDate monthOfPayment);
}
