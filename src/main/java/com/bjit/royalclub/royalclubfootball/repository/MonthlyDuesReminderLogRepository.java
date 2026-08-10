package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MonthlyDuesReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface MonthlyDuesReminderLogRepository extends JpaRepository<MonthlyDuesReminderLog, Long> {

    /**
     * Batch: reminders sent per player for a month on one channel, for the cap.
     * <p>
     * Scoped to a single channel because push and email each carry their own allowance - counting
     * both together would exhaust a cap of 3 partway through the 5th/10th/15th schedule.
     * Returns Object[] of {playerId, count}.
     */
    @Query("SELECT r.player.id, COUNT(r) FROM MonthlyDuesReminderLog r "
            + "WHERE r.monthOfPayment = :monthOfPayment AND r.player.id IN :playerIds "
            + "AND r.channel = :channel "
            + "GROUP BY r.player.id")
    List<Object[]> countByPlayersMonthAndChannel(
            @Param("playerIds") Collection<Long> playerIds,
            @Param("monthOfPayment") LocalDate monthOfPayment,
            @Param("channel") String channel);

    /**
     * Players already contacted on this channel within [dayStart, dayEnd).
     * <p>
     * Keeps a restarted scheduler, or an admin using the manual trigger on a run day, from sending
     * the same reminder twice in one day.
     */
    @Query("SELECT DISTINCT r.player.id FROM MonthlyDuesReminderLog r "
            + "WHERE r.monthOfPayment = :monthOfPayment AND r.channel = :channel "
            + "AND r.sentAt >= :dayStart AND r.sentAt < :dayEnd")
    List<Long> findPlayerIdsContactedBetween(
            @Param("monthOfPayment") LocalDate monthOfPayment,
            @Param("channel") String channel,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);
}
