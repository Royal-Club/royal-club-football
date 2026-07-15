package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TournamentReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentReminderLogRepository extends JpaRepository<TournamentReminderLog, Long> {

    /**
     * How many reminders have already been sent to this player for this tournament (across all channels).
     * Used to enforce the per-player reminder cap.
     */
    int countByTournamentIdAndPlayerId(Long tournamentId, Long playerId);
}
