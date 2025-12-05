package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long>, JpaSpecificationExecutor<Tournament> {

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

    @Query("SELECT t FROM Tournament t WHERE t.tournamentDate < :currentTournamentDate " +
            "ORDER BY t.tournamentDate DESC LIMIT 1")
    Tournament findMostRecentTournamentBefore(@Param("currentTournamentDate") java.time.LocalDateTime currentTournamentDate);

}
