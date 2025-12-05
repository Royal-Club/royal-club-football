package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MatchEvent;
import com.bjit.royalclub.royalclubfootball.enums.MatchEventType;
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

}
