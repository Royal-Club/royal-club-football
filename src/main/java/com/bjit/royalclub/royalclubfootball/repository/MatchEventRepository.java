package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MatchEvent;
import com.bjit.royalclub.royalclubfootball.enums.MatchEventType;
import com.bjit.royalclub.royalclubfootball.projection.PlayerStatisticsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {

    /**
     * Find all events for a specific match ordered by event time
     */
    @Query("SELECT me FROM MatchEvent me WHERE me.match.id = :matchId ORDER BY me.eventTime ASC")
    List<MatchEvent> findByMatchId(@Param("matchId") Long matchId);

    /**
     * Find events of specific type for a match
     */
    @Query("SELECT me FROM MatchEvent me WHERE me.match.id = :matchId AND me.eventType = :eventType ORDER BY me.eventTime ASC")
    List<MatchEvent> findByMatchIdAndEventType(@Param("matchId") Long matchId, @Param("eventType") MatchEventType eventType);

    /**
     * Find all events involving a specific player in a match
     */
    @Query("SELECT me FROM MatchEvent me WHERE me.match.id = :matchId AND me.player.id = :playerId ORDER BY me.eventTime ASC")
    List<MatchEvent> findByMatchIdAndPlayerId(@Param("matchId") Long matchId, @Param("playerId") Long playerId);

    /**
     * Find all events for a specific team in a match
     */
    @Query("SELECT me FROM MatchEvent me WHERE me.match.id = :matchId AND me.team.id = :teamId ORDER BY me.eventTime ASC")
    List<MatchEvent> findByMatchIdAndTeamId(@Param("matchId") Long matchId, @Param("teamId") Long teamId);

    /**
     * Find goal events for a specific team in a match
     */
    @Query("SELECT me FROM MatchEvent me WHERE me.match.id = :matchId AND me.team.id = :teamId AND me.eventType = 'GOAL' ORDER BY me.eventTime ASC")
    List<MatchEvent> findGoalsByMatchIdAndTeamId(@Param("matchId") Long matchId, @Param("teamId") Long teamId);

    /**
     * Find goals scored by a specific player in a match
     */
    @Query("SELECT COUNT(me) FROM MatchEvent me WHERE me.match.id = :matchId AND me.player.id = :playerId AND me.eventType = 'GOAL'")
    long countGoalsByMatchIdAndPlayerId(@Param("matchId") Long matchId, @Param("playerId") Long playerId);

    /**
     * Find all events in a tournament for a specific event type
     */
    @Query("SELECT me FROM MatchEvent me WHERE me.match.tournament.id = :tournamentId AND me.eventType = :eventType ORDER BY me.createdDate DESC")
    List<MatchEvent> findByTournamentIdAndEventType(@Param("tournamentId") Long tournamentId, @Param("eventType") MatchEventType eventType);

    /**
     * Find all events for a player across all matches in a tournament
     */
    @Query("SELECT me FROM MatchEvent me WHERE me.match.tournament.id = :tournamentId AND me.player.id = :playerId ORDER BY me.match.matchDate ASC, me.eventTime ASC")
    List<MatchEvent> findPlayerEventsByTournament(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    /**
     * Count specific type of events for a player in a match
     */
    @Query("SELECT COUNT(me) FROM MatchEvent me WHERE me.match.id = :matchId AND me.player.id = :playerId AND me.eventType = :eventType")
    long countEventsByMatchIdAndPlayerIdAndType(@Param("matchId") Long matchId,
                                                @Param("playerId") Long playerId,
                                                @Param("eventType") MatchEventType eventType);

    /**
     * Get aggregated player statistics from match events
     *
     * When tournamentId IS NULL: Returns ALL players in the system with their statistics from all tournaments
     * When tournamentId IS PROVIDED: Returns only players who participated in that specific tournament
     *                                (through team membership), with statistics from that tournament only
     *
     * Note:
     * - Returns ALL players from tournament teams, even if they have no match events (will show 0 stats)
     * - Assists are counted from relatedPlayer field in GOAL events, not from ASSIST event type
     * - matchesPlayed counts via subquery to avoid Cartesian product - counts COMPLETED matches where player's team participated
     * - This correctly counts all matches participated in, even if player had no events
     * - Position filtering is handled in service layer for cleaner code
     * - Players with no match events will show 0 for goals/assists/cards but may have matchesPlayed > 0
     */
    @Query("SELECT p.id as playerId, " +
            "COALESCE(SUM(CASE WHEN me.eventType = 'GOAL' AND me.player.id = p.id THEN 1 ELSE 0 END), 0) as goalsScored, " +
            "COALESCE(SUM(CASE WHEN me.eventType = 'GOAL' AND me.relatedPlayer.id = p.id THEN 1 ELSE 0 END), 0) as assists, " +
            "COALESCE((SELECT COUNT(DISTINCT m.id) FROM Match m " +
            "   JOIN TeamPlayer tp2 ON (m.homeTeam.id = tp2.team.id OR m.awayTeam.id = tp2.team.id) " +
            "   WHERE tp2.player.id = p.id " +
            "   AND m.matchStatus = 'COMPLETED' " +
            "   AND (:tournamentId IS NULL OR m.tournament.id = :tournamentId)), 0) as matchesPlayed, " +
            "COALESCE(SUM(CASE WHEN me.eventType = 'YELLOW_CARD' AND me.player.id = p.id THEN 1 ELSE 0 END), 0) as yellowCards, " +
            "COALESCE(SUM(CASE WHEN me.eventType = 'RED_CARD' AND me.player.id = p.id THEN 1 ELSE 0 END), 0) as redCards " +
            "FROM Player p " +
            "LEFT JOIN MatchEvent me ON (me.player.id = p.id OR me.relatedPlayer.id = p.id) " +
            "AND (:tournamentId IS NULL OR me.match.tournament.id = :tournamentId) " +
            "WHERE :tournamentId IS NULL OR EXISTS (" +
            "   SELECT 1 FROM TeamPlayer tp " +
            "   JOIN Team t ON t.id = tp.team.id " +
            "   WHERE tp.player.id = p.id AND t.tournament.id = :tournamentId" +
            ") " +
            "GROUP BY p.id")
    List<PlayerStatisticsProjection> findAggregatedPlayerStatisticsFromEvents(
            @Param("tournamentId") Long tournamentId
    );

}
