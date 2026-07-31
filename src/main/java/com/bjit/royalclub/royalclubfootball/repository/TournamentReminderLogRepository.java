package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TournamentReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TournamentReminderLogRepository extends JpaRepository<TournamentReminderLog, Long> {

    /**
     * How many reminders have already been sent to this player for this tournament (across all channels).
     * Used to enforce the per-player reminder cap.
     */
    int countByTournamentIdAndPlayerId(Long tournamentId, Long playerId);

    /**
     * Batch: count reminders per player for a tournament.
     * Returns Object[] of {playerId, count}.
     */
    @Query("SELECT r.player.id, COUNT(r) FROM TournamentReminderLog r " +
            "WHERE r.tournament.id = :tournamentId AND r.player.id IN :playerIds " +
            "GROUP BY r.player.id")
    List<Object[]> countByTournamentIdAndPlayerIds(
            @Param("tournamentId") Long tournamentId,
            @Param("playerIds") Collection<Long> playerIds);
}
