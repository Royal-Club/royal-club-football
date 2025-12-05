package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Match;
import com.bjit.royalclub.royalclubfootball.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    /**
     * Find all matches for a specific tournament ordered by match date
     */
    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId ORDER BY m.matchDate ASC")
    List<Match> findByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Find matches for a tournament by status
     */
    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId AND m.matchStatus = :status ORDER BY m.matchDate ASC")
    List<Match> findByTournamentIdAndStatus(@Param("tournamentId") Long tournamentId, @Param("status") MatchStatus status);

    /**
     * Find matches for a specific team in a tournament (home or away)
     */
    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId " +
           "AND (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId) " +
           "ORDER BY m.matchDate ASC")
    List<Match> findTeamMatchesByTournament(@Param("tournamentId") Long tournamentId, @Param("teamId") Long teamId);

    /**
     * Find matches by legacy round number (for knockout tournaments)
     */
    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId AND m.legacyRound = :round ORDER BY m.matchOrder ASC")
    List<Match> findByTournamentIdAndRound(@Param("tournamentId") Long tournamentId, @Param("round") Integer round);

    /**
     * Find matches by group name (for group stage tournaments)
     */
    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId AND m.groupName = :groupName ORDER BY m.matchDate ASC")
    List<Match> findByTournamentIdAndGroupName(@Param("tournamentId") Long tournamentId, @Param("groupName") String groupName);

    /**
     * Find matches within a date range for tournament
     */
    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId " +
           "AND m.matchDate BETWEEN :startDate AND :endDate " +
           "ORDER BY m.matchDate ASC")
    List<Match> findMatchesInDateRange(@Param("tournamentId") Long tournamentId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find ongoing matches for a specific tournament
     */
    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId AND m.matchStatus = 'ONGOING' ORDER BY m.matchDate DESC")
    List<Match> findOngoingMatches(@Param("tournamentId") Long tournamentId);

    /**
     * Count matches by status for a tournament
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.tournament.id = :tournamentId AND m.matchStatus = :status")
    long countByTournamentIdAndStatus(@Param("tournamentId") Long tournamentId, @Param("status") MatchStatus status);

    /**
     * Find match by tournament and match order (for fixture ordering)
     */
    @Query("SELECT m FROM Match m WHERE m.tournament.id = :tournamentId AND m.matchOrder = :matchOrder")
    Optional<Match> findByTournamentIdAndMatchOrder(@Param("tournamentId") Long tournamentId, @Param("matchOrder") Integer matchOrder);

    /**
     * Check if scheduled fixtures exist for a tournament (for duplicate prevention)
     */
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.tournament.id = :tournamentId AND m.matchStatus = 'SCHEDULED'")
    boolean hasScheduledFixtures(@Param("tournamentId") Long tournamentId);

    /**
     * Find team's matches within a date range (for conflict detection)
     */
    @Query("SELECT m FROM Match m WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId) " +
           "AND m.matchDate BETWEEN :startDateTime AND :endDateTime " +
           "AND m.matchStatus IN ('SCHEDULED', 'ONGOING') " +
           "ORDER BY m.matchDate ASC")
    List<Match> findTeamMatchesInTimeSlot(@Param("teamId") Long teamId,
                                         @Param("startDateTime") LocalDateTime startDateTime,
                                         @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Find venue matches within a date range (for venue availability)
     */
    @Query("SELECT m FROM Match m WHERE m.venue.id = :venueId " +
           "AND m.matchDate BETWEEN :startDateTime AND :endDateTime " +
           "AND m.matchStatus IN ('SCHEDULED', 'ONGOING') " +
           "ORDER BY m.matchDate ASC")
    List<Match> findVenueMatchesInTimeSlot(@Param("venueId") Long venueId,
                                          @Param("startDateTime") LocalDateTime startDateTime,
                                          @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * Get distinct legacy rounds for a tournament
     */
    @Query("SELECT DISTINCT m.legacyRound FROM Match m WHERE m.tournament.id = :tournamentId " +
           "AND m.legacyRound IS NOT NULL ORDER BY m.legacyRound ASC")
    List<Integer> findDistinctRoundsByTournament(@Param("tournamentId") Long tournamentId);

    /**
     * Get distinct groups for a tournament
     */
    @Query("SELECT DISTINCT m.groupName FROM Match m WHERE m.tournament.id = :tournamentId " +
           "AND m.groupName IS NOT NULL ORDER BY m.groupName ASC")
    List<String> findDistinctGroupsByTournament(@Param("tournamentId") Long tournamentId);

    /**
     * Check if a team has associated matches (home or away)
     */
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId")
    boolean existsByTeamId(@Param("teamId") Long teamId);

    /**
     * Find all matches where team is participating (home or away)
     */
    @Query("SELECT m FROM Match m WHERE m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId")
    List<Match> findAllByTeamId(@Param("teamId") Long teamId);

    // New queries for manual fixture system

    /**
     * Find all matches for a specific round
     */
    @Query("SELECT m FROM Match m WHERE m.round.id = :roundId ORDER BY m.matchOrder ASC, m.matchDate ASC")
    List<Match> findByRoundId(@Param("roundId") Long roundId);

    /**
     * Find all matches for a specific group
     */
    @Query("SELECT m FROM Match m WHERE m.group.id = :groupId ORDER BY m.matchDate ASC")
    List<Match> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Find placeholder matches (teams TBD)
     */
    @Query("SELECT m FROM Match m WHERE m.isPlaceholderMatch = true AND m.tournament.id = :tournamentId ORDER BY m.matchDate ASC")
    List<Match> findPlaceholderMatchesByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Find matches by round and status
     */
    @Query("SELECT m FROM Match m WHERE m.round.id = :roundId AND m.matchStatus = :status ORDER BY m.matchDate ASC")
    List<Match> findByRoundIdAndStatus(@Param("roundId") Long roundId, @Param("status") MatchStatus status);

    /**
     * Find matches by group and status
     */
    @Query("SELECT m FROM Match m WHERE m.group.id = :groupId AND m.matchStatus = :status ORDER BY m.matchDate ASC")
    List<Match> findByGroupIdAndStatus(@Param("groupId") Long groupId, @Param("status") MatchStatus status);

    /**
     * Count completed matches in a group
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.group.id = :groupId AND m.matchStatus = 'COMPLETED'")
    long countCompletedMatchesByGroupId(@Param("groupId") Long groupId);

    /**
     * Count total matches in a group
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.group.id = :groupId")
    long countMatchesByGroupId(@Param("groupId") Long groupId);

    /**
     * Count completed matches in a round
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.round.id = :roundId AND m.matchStatus = 'COMPLETED'")
    long countCompletedMatchesByRoundId(@Param("roundId") Long roundId);

    /**
     * Count total matches in a round
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.round.id = :roundId")
    long countMatchesByRoundId(@Param("roundId") Long roundId);

    /**
     * Check if all matches in a round are completed
     */
    @Query("SELECT CASE WHEN COUNT(m) = 0 THEN false " +
           "WHEN COUNT(m) = COUNT(CASE WHEN m.matchStatus = 'COMPLETED' THEN 1 END) THEN true " +
           "ELSE false END " +
           "FROM Match m WHERE m.round.id = :roundId")
    boolean areAllMatchesCompletedInRound(@Param("roundId") Long roundId);

    /**
     * Check if all matches in a group are completed
     */
    @Query("SELECT CASE WHEN COUNT(m) = 0 THEN false " +
           "WHEN COUNT(m) = COUNT(CASE WHEN m.matchStatus = 'COMPLETED' THEN 1 END) THEN true " +
           "ELSE false END " +
           "FROM Match m WHERE m.group.id = :groupId")
    boolean areAllMatchesCompletedInGroup(@Param("groupId") Long groupId);

    /**
     * Count all matches by tournament ID
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.tournament.id = :tournamentId")
    long countByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Count completed matches by tournament ID
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.tournament.id = :tournamentId AND m.matchStatus = 'COMPLETED'")
    long countCompletedByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Count all matches by round ID
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.round.id = :roundId")
    long countByRoundId(@Param("roundId") Long roundId);

    /**
     * Count completed matches by round ID
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.round.id = :roundId AND m.matchStatus = 'COMPLETED'")
    long countCompletedByRoundId(@Param("roundId") Long roundId);

    /**
     * Count all matches by group ID
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.group.id = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);

    /**
     * Count completed matches by group ID
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.group.id = :groupId AND m.matchStatus = 'COMPLETED'")
    long countCompletedByGroupId(@Param("groupId") Long groupId);

    /**
     * Find completed matches by group ID
     */
    @Query("SELECT m FROM Match m WHERE m.group.id = :groupId AND m.matchStatus = 'COMPLETED' ORDER BY m.matchDate ASC")
    List<Match> findCompletedByGroupId(@Param("groupId") Long groupId);

    /**
     * Count matches by group ID and team ID (team is home or away)
     */
    @Query("SELECT COUNT(m) FROM Match m WHERE m.group.id = :groupId AND (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId)")
    long countByGroupIdAndTeamId(@Param("groupId") Long groupId, @Param("teamId") Long teamId);

}
