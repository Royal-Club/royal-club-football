package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TournamentReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TournamentReminderLogRepository extends JpaRepository<TournamentReminderLog, Long> {

    /**
     * Batch: reminders sent per player on one channel, for the cap.
     * <p>
     * Scoped to a single channel because email and push each carry their own allowance - counting
     * across both would exhaust a cap of 3 halfway through the D-2/D-1/match-day schedule. Invitations
     * are excluded so the creation announcement never eats into a player's reminder budget.
     * Returns Object[] of {playerId, count}.
     */
    @Query("SELECT r.player.id, COUNT(r) FROM TournamentReminderLog r "
            + "WHERE r.tournament.id = :tournamentId AND r.player.id IN :playerIds "
            + "AND r.channel = :channel AND r.reminderType = :reminderType "
            + "GROUP BY r.player.id")
    List<Object[]> countByTournamentPlayersChannelAndType(
            @Param("tournamentId") Long tournamentId,
            @Param("playerIds") Collection<Long> playerIds,
            @Param("channel") String channel,
            @Param("reminderType") String reminderType);

    /**
     * Players already contacted on this channel within [dayStart, dayEnd).
     * <p>
     * Enforces one contact per player per channel per calendar day. This is what makes a creation
     * invitation suppress that same day's reminder, and what stops a scheduler re-run after a restart
     * from double-sending. Deliberately ignores reminder type: an invite occupies the day just as a
     * reminder does.
     */
    @Query("SELECT DISTINCT r.player.id FROM TournamentReminderLog r "
            + "WHERE r.tournament.id = :tournamentId AND r.channel = :channel "
            + "AND r.sentAt >= :dayStart AND r.sentAt < :dayEnd")
    List<Long> findPlayerIdsContactedBetween(
            @Param("tournamentId") Long tournamentId,
            @Param("channel") String channel,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);
}
