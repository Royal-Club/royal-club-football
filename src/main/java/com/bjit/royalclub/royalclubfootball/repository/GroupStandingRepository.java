package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.GroupStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GroupStandingRepository extends JpaRepository<GroupStanding, Long> {

    /**
     * Find all standings for a specific group ordered by position
     */
    @Query("SELECT s FROM GroupStanding s WHERE s.group.id = :groupId ORDER BY s.position ASC NULLS LAST")
    List<GroupStanding> findByGroupIdOrderByPosition(@Param("groupId") Long groupId);

    /**
     * Find all standings for a specific group ordered by points, goal difference, goals for
     */
    @Query("SELECT s FROM GroupStanding s WHERE s.group.id = :groupId " +
           "ORDER BY s.points DESC, s.goalDifference DESC, s.goalsFor DESC, s.team.teamName ASC")
    List<GroupStanding> findByGroupIdOrderByRankingCriteria(@Param("groupId") Long groupId);

    /**
     * Find standing for a team in a group
     */
    @Query("SELECT s FROM GroupStanding s WHERE s.group.id = :groupId AND s.team.id = :teamId")
    Optional<GroupStanding> findByGroupIdAndTeamId(@Param("groupId") Long groupId, @Param("teamId") Long teamId);

    /**
     * Find teams that have advanced from a group
     */
    @Query("SELECT s FROM GroupStanding s WHERE s.group.id = :groupId AND s.isAdvanced = true ORDER BY s.position ASC")
    List<GroupStanding> findAdvancedTeamsByGroupId(@Param("groupId") Long groupId);

    /**
     * Find top N teams from a group
     */
    @Query("SELECT s FROM GroupStanding s WHERE s.group.id = :groupId " +
           "ORDER BY s.points DESC, s.goalDifference DESC, s.goalsFor DESC")
    List<GroupStanding> findTopNTeamsByGroupId(@Param("groupId") Long groupId);

    /**
     * Find standings for all groups in a round
     */
    @Query("SELECT s FROM GroupStanding s WHERE s.group.round.id = :roundId " +
           "ORDER BY s.group.groupName, s.position ASC NULLS LAST")
    List<GroupStanding> findByRoundIdOrderByGroupAndPosition(@Param("roundId") Long roundId);

    /**
     * Check if standing exists for team in group
     */
    @Query("SELECT COUNT(s) > 0 FROM GroupStanding s WHERE s.group.id = :groupId AND s.team.id = :teamId")
    boolean existsByGroupIdAndTeamId(@Param("groupId") Long groupId, @Param("teamId") Long teamId);

    /**
     * Delete all standings for a group (for recalculation)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM GroupStanding s WHERE s.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);

    /**
     * Count teams in a group standings
     */
    @Query("SELECT COUNT(s) FROM GroupStanding s WHERE s.group.id = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);

    /**
     * Find all standings for a specific group (simple query)
     */
    @Query("SELECT s FROM GroupStanding s WHERE s.group.id = :groupId")
    List<GroupStanding> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Delete standing for a specific team in a group
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM GroupStanding s WHERE s.group.id = :groupId AND s.team.id = :teamId")
    void deleteByGroupIdAndTeamId(@Param("groupId") Long groupId, @Param("teamId") Long teamId);
}
