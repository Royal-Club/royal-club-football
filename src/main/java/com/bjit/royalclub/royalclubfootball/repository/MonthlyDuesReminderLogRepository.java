package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MonthlyDuesReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface MonthlyDuesReminderLogRepository extends JpaRepository<MonthlyDuesReminderLog, Long> {

    /**
     * How many dues reminders have already been sent to this player for this month (across all channels).
     * Used to enforce the per-player, per-month reminder cap.
     */
    int countByPlayerIdAndMonthOfPayment(Long playerId, LocalDate monthOfPayment);
}
