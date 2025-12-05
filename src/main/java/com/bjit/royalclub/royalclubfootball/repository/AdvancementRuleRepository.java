package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.AdvancementRule;
import com.bjit.royalclub.royalclubfootball.enums.AdvancementRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvancementRuleRepository extends JpaRepository<AdvancementRule, Long> {

    /**
     * Find all advancement rules from a source round
     */
    @Query("SELECT ar FROM AdvancementRule ar WHERE ar.sourceRound.id = :sourceRoundId ORDER BY ar.priorityOrder ASC NULLS LAST")
    List<AdvancementRule> findBySourceRoundId(@Param("sourceRoundId") Long sourceRoundId);

    /**
     * Find all advancement rules to a target round
     */
    @Query("SELECT ar FROM AdvancementRule ar WHERE ar.targetRound.id = :targetRoundId ORDER BY ar.priorityOrder ASC NULLS LAST")
    List<AdvancementRule> findByTargetRoundId(@Param("targetRoundId") Long targetRoundId);

    /**
     * Find advancement rules from a specific source group
     */
    @Query("SELECT ar FROM AdvancementRule ar WHERE ar.sourceGroup.id = :sourceGroupId ORDER BY ar.priorityOrder ASC NULLS LAST")
    List<AdvancementRule> findBySourceGroupId(@Param("sourceGroupId") Long sourceGroupId);

    /**
     * Find advancement rules by type for a source round
     */
    @Query("SELECT ar FROM AdvancementRule ar WHERE ar.sourceRound.id = :sourceRoundId AND ar.ruleType = :ruleType")
    List<AdvancementRule> findBySourceRoundIdAndType(@Param("sourceRoundId") Long sourceRoundId, @Param("ruleType") AdvancementRuleType ruleType);

    /**
     * Find advancement rules between two rounds
     */
    @Query("SELECT ar FROM AdvancementRule ar WHERE ar.sourceRound.id = :sourceRoundId AND ar.targetRound.id = :targetRoundId ORDER BY ar.priorityOrder ASC NULLS LAST")
    List<AdvancementRule> findBySourceAndTargetRounds(@Param("sourceRoundId") Long sourceRoundId, @Param("targetRoundId") Long targetRoundId);

    /**
     * Check if advancement rule exists between rounds
     */
    @Query("SELECT COUNT(ar) > 0 FROM AdvancementRule ar WHERE ar.sourceRound.id = :sourceRoundId AND ar.targetRound.id = :targetRoundId")
    boolean existsBySourceAndTargetRounds(@Param("sourceRoundId") Long sourceRoundId, @Param("targetRoundId") Long targetRoundId);

    /**
     * Delete all advancement rules for a source round
     */
    @Query("DELETE FROM AdvancementRule ar WHERE ar.sourceRound.id = :sourceRoundId")
    void deleteBySourceRoundId(@Param("sourceRoundId") Long sourceRoundId);

    /**
     * Delete all advancement rules for a target round
     */
    @Query("DELETE FROM AdvancementRule ar WHERE ar.targetRound.id = :targetRoundId")
    void deleteByTargetRoundId(@Param("targetRoundId") Long targetRoundId);

    /**
     * Find all advancement rules from a source round ordered by priority
     */
    @Query("SELECT ar FROM AdvancementRule ar WHERE ar.sourceRound.id = :sourceRoundId ORDER BY ar.priorityOrder ASC NULLS LAST")
    List<AdvancementRule> findBySourceRoundIdOrderByPriority(@Param("sourceRoundId") Long sourceRoundId);
}
