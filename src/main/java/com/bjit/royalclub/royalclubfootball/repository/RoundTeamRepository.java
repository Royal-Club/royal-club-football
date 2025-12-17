package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.RoundTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RoundTeamRepository extends JpaRepository<RoundTeam, Long> {

    /**
     * Find all teams in a specific round (for direct knockout rounds)
     */
    @Query("SELECT rt FROM RoundTeam rt WHERE rt.round.id = :roundId ORDER BY rt.seedPosition ASC NULLS LAST")
    List<RoundTeam> findByRoundIdOrderBySeed(@Param("roundId") Long roundId);

    /**
     * Find team assignment in a round
     */
    @Query("SELECT rt FROM RoundTeam rt WHERE rt.round.id = :roundId AND rt.team.id = :teamId")
    Optional<RoundTeam> findByRoundIdAndTeamId(@Param("roundId") Long roundId, @Param("teamId") Long teamId);

    /**
     * Find team by seed position in a round
     */
    @Query("SELECT rt FROM RoundTeam rt WHERE rt.round.id = :roundId AND rt.seedPosition = :seedPosition")
    Optional<RoundTeam> findByRoundIdAndSeedPosition(@Param("roundId") Long roundId, @Param("seedPosition") Integer seedPosition);

    /**
     * Find placeholder teams in a round
     */
    @Query("SELECT rt FROM RoundTeam rt WHERE rt.round.id = :roundId AND rt.isPlaceholder = true")
    List<RoundTeam> findPlaceholdersByRoundId(@Param("roundId") Long roundId);

    /**
     * Find actual (non-placeholder) teams in a round
     */
    @Query("SELECT rt FROM RoundTeam rt WHERE rt.round.id = :roundId AND rt.isPlaceholder = false ORDER BY rt.seedPosition ASC NULLS LAST")
    List<RoundTeam> findActualTeamsByRoundId(@Param("roundId") Long roundId);

    /**
     * Count teams in a round
     */
    @Query("SELECT COUNT(rt) FROM RoundTeam rt WHERE rt.round.id = :roundId")
    long countByRoundId(@Param("roundId") Long roundId);

    /**
     * Count actual teams in a round (excluding placeholders)
     */
    @Query("SELECT COUNT(rt) FROM RoundTeam rt WHERE rt.round.id = :roundId AND rt.isPlaceholder = false")
    long countActualTeamsByRoundId(@Param("roundId") Long roundId);

    /**
     * Check if team exists in round
     */
    @Query("SELECT COUNT(rt) > 0 FROM RoundTeam rt WHERE rt.round.id = :roundId AND rt.team.id = :teamId")
    boolean existsByRoundIdAndTeamId(@Param("roundId") Long roundId, @Param("teamId") Long teamId);

    /**
     * Check if seed position is taken in a round
     */
    @Query("SELECT COUNT(rt) > 0 FROM RoundTeam rt WHERE rt.round.id = :roundId AND rt.seedPosition = :seedPosition")
    boolean existsByRoundIdAndSeedPosition(@Param("roundId") Long roundId, @Param("seedPosition") Integer seedPosition);

    /**
     * Delete team from round
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM RoundTeam rt WHERE rt.round.id = :roundId AND rt.team.id = :teamId")
    void deleteByRoundIdAndTeamId(@Param("roundId") Long roundId, @Param("teamId") Long teamId);

    /**
     * Find all teams in a round (simple query)
     */
    @Query("SELECT rt FROM RoundTeam rt WHERE rt.round.id = :roundId")
    List<RoundTeam> findByRoundId(@Param("roundId") Long roundId);
}
