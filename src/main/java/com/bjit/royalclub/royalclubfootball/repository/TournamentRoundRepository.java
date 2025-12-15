package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TournamentRound;
import com.bjit.royalclub.royalclubfootball.enums.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TournamentRoundRepository extends JpaRepository<TournamentRound, Long> {

    /**
     * Find all rounds for a specific tournament ordered by sequence
     */
    @Query("SELECT r FROM TournamentRound r WHERE r.tournament.id = :tournamentId ORDER BY r.sequenceOrder ASC")
    List<TournamentRound> findByTournamentIdOrderBySequence(@Param("tournamentId") Long tournamentId);

    /**
     * Find round by tournament and round number
     */
    @Query("SELECT r FROM TournamentRound r WHERE r.tournament.id = :tournamentId AND r.roundNumber = :roundNumber")
    Optional<TournamentRound> findByTournamentIdAndRoundNumber(@Param("tournamentId") Long tournamentId, @Param("roundNumber") Integer roundNumber);

    /**
     * Find rounds by status for a tournament
     */
    @Query("SELECT r FROM TournamentRound r WHERE r.tournament.id = :tournamentId AND r.status = :status ORDER BY r.sequenceOrder ASC")
    List<TournamentRound> findByTournamentIdAndStatus(@Param("tournamentId") Long tournamentId, @Param("status") RoundStatus status);

    /**
     * Check if a round exists for tournament and round number
     */
    @Query("SELECT COUNT(r) > 0 FROM TournamentRound r WHERE r.tournament.id = :tournamentId AND r.roundNumber = :roundNumber")
    boolean existsByTournamentIdAndRoundNumber(@Param("tournamentId") Long tournamentId, @Param("roundNumber") Integer roundNumber);

    /**
     * Count rounds in a tournament
     */
    @Query("SELECT COUNT(r) FROM TournamentRound r WHERE r.tournament.id = :tournamentId")
    long countByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Find next round after current sequence order
     */
    @Query("SELECT r FROM TournamentRound r WHERE r.tournament.id = :tournamentId " +
           "AND r.sequenceOrder > :sequenceOrder ORDER BY r.sequenceOrder ASC")
    Optional<TournamentRound> findNextRoundBySequence(@Param("tournamentId") Long tournamentId, @Param("sequenceOrder") Integer sequenceOrder);

    /**
     * Find previous round before current sequence order
     * Returns the round with the highest sequenceOrder that is less than the given sequenceOrder
     * Uses subquery to ensure only one result is returned even if there are duplicate sequenceOrders
     * Returns List to handle edge cases where duplicates might exist (should be fixed by backend auto-calculation)
     */
    @Query("SELECT r FROM TournamentRound r WHERE r.tournament.id = :tournamentId " +
           "AND r.sequenceOrder = (SELECT MAX(r2.sequenceOrder) FROM TournamentRound r2 " +
           "WHERE r2.tournament.id = :tournamentId AND r2.sequenceOrder < :sequenceOrder) " +
           "ORDER BY r.id ASC")
    List<TournamentRound> findPreviousRoundBySequence(@Param("tournamentId") Long tournamentId, @Param("sequenceOrder") Integer sequenceOrder);

    /**
     * Check if a round exists for tournament and sequence order
     */
    @Query("SELECT COUNT(r) > 0 FROM TournamentRound r WHERE r.tournament.id = :tournamentId AND r.sequenceOrder = :sequenceOrder")
    boolean existsByTournamentIdAndSequenceOrder(@Param("tournamentId") Long tournamentId, @Param("sequenceOrder") Integer sequenceOrder);
}
