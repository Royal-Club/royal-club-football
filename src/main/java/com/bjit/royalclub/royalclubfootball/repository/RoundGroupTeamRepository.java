package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.RoundGroupTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoundGroupTeamRepository extends JpaRepository<RoundGroupTeam, Long> {

    /**
     * Find all teams in a specific group
     */
    @Query("SELECT gt FROM RoundGroupTeam gt WHERE gt.group.id = :groupId")
    List<RoundGroupTeam> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Find team assignment in a group
     */
    @Query("SELECT gt FROM RoundGroupTeam gt WHERE gt.group.id = :groupId AND gt.team.id = :teamId")
    Optional<RoundGroupTeam> findByGroupIdAndTeamId(@Param("groupId") Long groupId, @Param("teamId") Long teamId);

    /**
     * Find placeholder teams in a group
     */
    @Query("SELECT gt FROM RoundGroupTeam gt WHERE gt.group.id = :groupId AND gt.isPlaceholder = true")
    List<RoundGroupTeam> findPlaceholdersByGroupId(@Param("groupId") Long groupId);

    /**
     * Find actual (non-placeholder) teams in a group
     */
    @Query("SELECT gt FROM RoundGroupTeam gt WHERE gt.group.id = :groupId AND gt.isPlaceholder = false")
    List<RoundGroupTeam> findActualTeamsByGroupId(@Param("groupId") Long groupId);

    /**
     * Count teams in a group
     */
    @Query("SELECT COUNT(gt) FROM RoundGroupTeam gt WHERE gt.group.id = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);

    /**
     * Count actual teams in a group (excluding placeholders)
     */
    @Query("SELECT COUNT(gt) FROM RoundGroupTeam gt WHERE gt.group.id = :groupId AND gt.isPlaceholder = false")
    long countActualTeamsByGroupId(@Param("groupId") Long groupId);

    /**
     * Check if team exists in group
     */
    @Query("SELECT COUNT(gt) > 0 FROM RoundGroupTeam gt WHERE gt.group.id = :groupId AND gt.team.id = :teamId")
    boolean existsByGroupIdAndTeamId(@Param("groupId") Long groupId, @Param("teamId") Long teamId);

    /**
     * Find all groups that contain a specific team in a round
     */
    @Query("SELECT gt FROM RoundGroupTeam gt WHERE gt.group.round.id = :roundId AND gt.team.id = :teamId")
    List<RoundGroupTeam> findByRoundIdAndTeamId(@Param("roundId") Long roundId, @Param("teamId") Long teamId);

    /**
     * Delete team from group
     */
    @Query("DELETE FROM RoundGroupTeam gt WHERE gt.group.id = :groupId AND gt.team.id = :teamId")
    void deleteByGroupIdAndTeamId(@Param("groupId") Long groupId, @Param("teamId") Long teamId);
}
