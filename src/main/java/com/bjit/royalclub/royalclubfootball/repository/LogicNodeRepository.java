package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.LogicNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogicNodeRepository extends JpaRepository<LogicNode, Long> {

    /**
     * Find all logic nodes for a tournament
     */
    @Query("SELECT n FROM LogicNode n WHERE n.tournament.id = :tournamentId ORDER BY n.priorityOrder ASC NULLS LAST, n.id ASC")
    List<LogicNode> findByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Find active logic nodes for a tournament
     */
    @Query("SELECT n FROM LogicNode n WHERE n.tournament.id = :tournamentId AND n.isActive = true ORDER BY n.priorityOrder ASC NULLS LAST, n.id ASC")
    List<LogicNode> findActiveByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Find logic nodes with source round
     */
    @Query("SELECT n FROM LogicNode n WHERE n.sourceRound.id = :roundId AND n.isActive = true ORDER BY n.priorityOrder ASC NULLS LAST")
    List<LogicNode> findBySourceRoundId(@Param("roundId") Long roundId);

    /**
     * Find logic nodes with source group
     */
    @Query("SELECT n FROM LogicNode n WHERE n.sourceGroup.id = :groupId AND n.isActive = true ORDER BY n.priorityOrder ASC NULLS LAST")
    List<LogicNode> findBySourceGroupId(@Param("groupId") Long groupId);

    /**
     * Find logic nodes with target round
     */
    @Query("SELECT n FROM LogicNode n WHERE n.targetRound.id = :roundId ORDER BY n.priorityOrder ASC NULLS LAST")
    List<LogicNode> findByTargetRoundId(@Param("roundId") Long roundId);

    /**
     * Find auto-execute logic nodes for a source round
     */
    @Query("SELECT n FROM LogicNode n WHERE n.sourceRound.id = :roundId AND n.isActive = true AND n.autoExecute = true ORDER BY n.priorityOrder ASC NULLS LAST")
    List<LogicNode> findAutoExecuteBySourceRoundId(@Param("roundId") Long roundId);

    /**
     * Find auto-execute logic nodes for a source group
     */
    @Query("SELECT n FROM LogicNode n WHERE n.sourceGroup.id = :groupId AND n.isActive = true AND n.autoExecute = true ORDER BY n.priorityOrder ASC NULLS LAST")
    List<LogicNode> findAutoExecuteBySourceGroupId(@Param("groupId") Long groupId);

    /**
     * Check if logic node exists for tournament
     */
    @Query("SELECT COUNT(n) > 0 FROM LogicNode n WHERE n.tournament.id = :tournamentId")
    boolean existsByTournamentId(@Param("tournamentId") Long tournamentId);
}

