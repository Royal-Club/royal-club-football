package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.RoundGroup;
import com.bjit.royalclub.royalclubfootball.enums.RoundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoundGroupRepository extends JpaRepository<RoundGroup, Long> {

    /**
     * Find all groups for a specific round
     */
    @Query("SELECT g FROM RoundGroup g WHERE g.round.id = :roundId ORDER BY g.groupName ASC")
    List<RoundGroup> findByRoundId(@Param("roundId") Long roundId);

    /**
     * Find group by round and group name
     */
    @Query("SELECT g FROM RoundGroup g WHERE g.round.id = :roundId AND g.groupName = :groupName")
    Optional<RoundGroup> findByRoundIdAndGroupName(@Param("roundId") Long roundId, @Param("groupName") String groupName);

    /**
     * Find groups by status for a round
     */
    @Query("SELECT g FROM RoundGroup g WHERE g.round.id = :roundId AND g.status = :status ORDER BY g.groupName ASC")
    List<RoundGroup> findByRoundIdAndStatus(@Param("roundId") Long roundId, @Param("status") RoundStatus status);

    /**
     * Check if group exists for round and group name
     */
    @Query("SELECT COUNT(g) > 0 FROM RoundGroup g WHERE g.round.id = :roundId AND g.groupName = :groupName")
    boolean existsByRoundIdAndGroupName(@Param("roundId") Long roundId, @Param("groupName") String groupName);

    /**
     * Count groups in a round
     */
    @Query("SELECT COUNT(g) FROM RoundGroup g WHERE g.round.id = :roundId")
    long countByRoundId(@Param("roundId") Long roundId);

    /**
     * Find groups for a tournament (across all rounds)
     */
    @Query("SELECT g FROM RoundGroup g WHERE g.round.tournament.id = :tournamentId ORDER BY g.round.sequenceOrder, g.groupName")
    List<RoundGroup> findByTournamentId(@Param("tournamentId") Long tournamentId);
}
