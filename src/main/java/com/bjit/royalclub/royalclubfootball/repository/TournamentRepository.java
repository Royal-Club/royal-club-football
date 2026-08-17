package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long>, JpaSpecificationExecutor<Tournament> {

    @Query("SELECT DISTINCT t FROM Tournament t LEFT JOIN FETCH t.venue")
    List<Tournament> findAllWithVenue();

    /**
     * Find all active tournaments that have no matches (fixtures not generated yet)
     * These should be concluded based on tournament date
     */
    @Query("SELECT t FROM Tournament t WHERE t.isActive = true " +
            "AND NOT EXISTS (SELECT m FROM Match m WHERE m.tournament.id = t.id)")
    List<Tournament> findActiveTournamentsWithoutMatches();

    /**
     * Find all active tournaments that have matches
     */
    @Query("SELECT DISTINCT t FROM Tournament t " +
            "WHERE t.isActive = true " +
            "AND EXISTS (SELECT m FROM Match m WHERE m.tournament.id = t.id)")
    List<Tournament> findActiveTournamentsWithMatches();

    /**
     * Check if tournament has any ongoing or paused matches
     */
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.tournament.id = :tournamentId " +
            "AND m.matchStatus IN ('ONGOING', 'PAUSED')")
    boolean hasOngoingMatches(@Param("tournamentId") Long tournamentId);

    /**
     * Check if all matches are completed or canceled (none scheduled/ongoing/paused)
     */
    @Query("SELECT COUNT(m) = 0 FROM Match m WHERE m.tournament.id = :tournamentId " +
            "AND m.matchStatus IN ('SCHEDULED', 'ONGOING', 'PAUSED')")
    boolean allMatchesFinishedOrCanceled(@Param("tournamentId") Long tournamentId);

    Tournament findTopByOrderByTournamentDateDesc();

    /**
     * Active, still-upcoming tournaments whose date falls within the reminder window [from, to].
     * Used by the RSVP reminder scheduler to find matches ~1-2 days away.
     */
    @Query("SELECT t FROM Tournament t WHERE t.isActive = true AND t.tournamentStatus = "
            + "com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.UPCOMING "
            + "AND t.tournamentDate BETWEEN :from AND :to")
    List<Tournament> findUpcomingWithinWindow(@Param("from") java.time.LocalDateTime from,
                                              @Param("to") java.time.LocalDateTime to);

    @Query("SELECT t FROM Tournament t WHERE t.tournamentStatus IN ('UPCOMING', 'ONGOING') " +
            "ORDER BY CASE t.tournamentStatus WHEN 'ONGOING' THEN 0 ELSE 1 END ASC, t.tournamentDate ASC LIMIT 1")
    Tournament findNextActiveTournament();

    @Query("SELECT t FROM Tournament t WHERE t.tournamentDate < :currentTournamentDate " +
            "ORDER BY t.tournamentDate DESC LIMIT 1")
    Tournament findMostRecentTournamentBefore(@Param("currentTournamentDate") java.time.LocalDateTime currentTournamentDate);

    /**
     * Ids of the tournaments immediately preceding the given date, most recent first.
     * <p>
     * Backs the goalkeeper cooldown, which rests anyone who kept goal in the last few tournaments.
     * The ordering carries meaning to the caller: index 0 is the tournament just gone, so a lower
     * index means a more recent turn in goal and a longer wait before coming back up the queue.
     */
    @Query("SELECT t.id FROM Tournament t WHERE t.tournamentDate < :currentTournamentDate " +
            "ORDER BY t.tournamentDate DESC")
    List<Long> findRecentTournamentIdsBefore(
            @Param("currentTournamentDate") java.time.LocalDateTime currentTournamentDate,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Find all tournaments ordered by tournament date descending
     */
    @Query("SELECT t FROM Tournament t ORDER BY t.tournamentDate DESC")
    List<Tournament> findAllOrderByTournamentDateDesc();

    /**
     * Find tournaments by year, ordered by tournament date descending
     * @param year The year to filter tournaments (e.g., 2025)
     */
    @Query("SELECT t FROM Tournament t WHERE YEAR(t.tournamentDate) = :year ORDER BY t.tournamentDate DESC")
    List<Tournament> findByYearOrderByTournamentDateDesc(@Param("year") int year);

    /**
     * Tournaments that have already taken place, oldest first. Used as the
     * timeline the attendance report is measured against.
     */
    @Query("SELECT t FROM Tournament t WHERE t.tournamentDate < :asOf ORDER BY t.tournamentDate ASC")
    List<Tournament> findHeldTournaments(@Param("asOf") java.time.LocalDateTime asOf);

    /**
     * {@link #findHeldTournaments} narrowed to a single calendar year.
     */
    @Query("SELECT t FROM Tournament t WHERE t.tournamentDate < :asOf AND YEAR(t.tournamentDate) = :year "
            + "ORDER BY t.tournamentDate ASC")
    List<Tournament> findHeldTournamentsByYear(@Param("asOf") java.time.LocalDateTime asOf,
                                               @Param("year") int year);

    /**
     * Find distinct years where tournaments exist
     * Returns years in descending order (newest first)
     */
    @Query("SELECT DISTINCT YEAR(t.tournamentDate) FROM Tournament t ORDER BY YEAR(t.tournamentDate) DESC")
    List<Integer> findDistinctYears();

        @Modifying
        @Transactional
        @Query("UPDATE Tournament t SET t.defaultTournament = false WHERE t.id <> :tournamentId AND t.defaultTournament = true")
        int clearDefaultTournamentExcept(@Param("tournamentId") Long tournamentId);

        @Modifying
        @Transactional
        @Query("UPDATE Tournament t SET t.defaultTournament = false WHERE t.defaultTournament = true")
        int clearDefaultTournament();

}
