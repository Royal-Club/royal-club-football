package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MatchStatistics;
import com.bjit.royalclub.royalclubfootball.projection.PlayerStatisticsProjection;
import com.bjit.royalclub.royalclubfootball.projection.TeamInfoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchStatisticsRepository extends JpaRepository<MatchStatistics, Long> {

    /**
     * Find statistics for all players in a specific match
     */
    @Query("SELECT ms FROM MatchStatistics ms WHERE ms.match.id = :matchId ORDER BY ms.goalsScored DESC")
    List<MatchStatistics> findByMatchId(@Param("matchId") Long matchId);

    /**
     * Find statistics for a specific player in a specific match
     */
    @Query("SELECT ms FROM MatchStatistics ms WHERE ms.match.id = :matchId AND ms.player.id = :playerId")
    Optional<MatchStatistics> findByMatchIdAndPlayerId(@Param("matchId") Long matchId, @Param("playerId") Long playerId);

    /**
     * Find statistics for a team in a specific match
     */
    @Query("SELECT ms FROM MatchStatistics ms WHERE ms.match.id = :matchId AND ms.team.id = :teamId ORDER BY ms.goalsScored DESC")
    List<MatchStatistics> findByMatchIdAndTeamId(@Param("matchId") Long matchId, @Param("teamId") Long teamId);

    /**
     * Find all statistics for a player in a tournament
     */
    @Query("SELECT ms FROM MatchStatistics ms WHERE ms.match.tournament.id = :tournamentId AND ms.player.id = :playerId ORDER BY ms.match.matchDate ASC")
    List<MatchStatistics> findPlayerStatsByTournament(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    /**
     * Get top scorers in a tournament
     */
    @Query("SELECT ms FROM MatchStatistics ms WHERE ms.match.tournament.id = :tournamentId " +
           "GROUP BY ms.player.id " +
           "ORDER BY SUM(ms.goalsScored) DESC")
    List<MatchStatistics> findTopScorersByTournament(@Param("tournamentId") Long tournamentId);

    /**
     * Get top assist providers in a tournament
     */
    @Query("SELECT ms FROM MatchStatistics ms WHERE ms.match.tournament.id = :tournamentId " +
           "GROUP BY ms.player.id " +
           "ORDER BY SUM(ms.assists) DESC")
    List<MatchStatistics> findTopAssistProvidersByTournament(@Param("tournamentId") Long tournamentId);

    /**
     * Calculate total goals for a team in a tournament
     */
    @Query("SELECT SUM(ms.goalsScored) FROM MatchStatistics ms WHERE ms.match.tournament.id = :tournamentId AND ms.team.id = :teamId")
    Long getTotalGoalsByTeamInTournament(@Param("tournamentId") Long tournamentId, @Param("teamId") Long teamId);

    /**
     * Find statistics for all matches in a tournament
     */
    @Query("SELECT ms FROM MatchStatistics ms WHERE ms.match.tournament.id = :tournamentId ORDER BY ms.match.matchDate ASC, ms.goalsScored DESC")
    List<MatchStatistics> findByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Count total matches played by a player in a tournament
     */
    @Query("SELECT COUNT(DISTINCT ms.match.id) FROM MatchStatistics ms WHERE ms.match.tournament.id = :tournamentId AND ms.player.id = :playerId")
    long countMatchesPlayedByPlayerInTournament(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    /**
     * Get total red cards for a player in a tournament
     */
    @Query("SELECT SUM(ms.redCards) FROM MatchStatistics ms WHERE ms.match.tournament.id = :tournamentId AND ms.player.id = :playerId")
    Integer getTotalRedCardsByPlayerInTournament(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    /**
     * Get total yellow cards for a player in a tournament
     */
    @Query("SELECT SUM(ms.yellowCards) FROM MatchStatistics ms WHERE ms.match.tournament.id = :tournamentId AND ms.player.id = :playerId")
    Integer getTotalYellowCardsByPlayerInTournament(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    /**
     * Delete all statistics for a specific match
     * Used when re-aggregating statistics from match events
     */
    void deleteByMatchId(Long matchId);

    /**
     * Get aggregated player statistics across all tournaments or specific tournament
     * Returns projection with type-safe access to statistics
     * Returns ALL players (even those without statistics) with stats summed or 0
     */
    @Query("SELECT p.id as playerId, " +
           "COALESCE(SUM(ms.goalsScored), 0) as goalsScored, " +
           "COALESCE(SUM(ms.assists), 0) as assists, " +
           "COALESCE(COUNT(DISTINCT ms.match.id), 0) as matchesPlayed, " +
           "COALESCE(SUM(ms.minutesPlayed), 0) as minutesPlayed, " +
           "COALESCE(SUM(ms.yellowCards), 0) as yellowCards, " +
           "COALESCE(SUM(ms.redCards), 0) as redCards " +
           "FROM Player p " +
           "LEFT JOIN MatchStatistics ms ON ms.player.id = p.id " +
           "AND (:tournamentId IS NULL OR ms.match.tournament.id = :tournamentId) " +
           "WHERE (:position IS NULL OR CAST(p.position AS string) = :position) " +
           "GROUP BY p.id")
    List<PlayerStatisticsProjection> findAggregatedPlayerStatistics(
        @Param("tournamentId") Long tournamentId,
        @Param("position") String position
    );

    /**
     * Get all teams a player has played for with tournament information
     * Returns projection with type-safe access to team info
     */
    @Query("SELECT DISTINCT ms.team.id as teamId, " +
           "ms.team.teamName as teamName, " +
           "ms.match.tournament.id as tournamentId, " +
           "ms.match.tournament.name as tournamentName " +
           "FROM MatchStatistics ms " +
           "WHERE ms.player.id = :playerId " +
           "AND (:tournamentId IS NULL OR ms.match.tournament.id = :tournamentId) " +
           "ORDER BY ms.match.tournament.tournamentDate DESC")
    List<TeamInfoProjection> findTeamsByPlayerId(
        @Param("playerId") Long playerId,
        @Param("tournamentId") Long tournamentId
    );

}
