package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TeamFormation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamFormationRepository extends JpaRepository<TeamFormation, Long> {

    /** The team's default line-up — the one with no match attached. */
    @Query("SELECT f FROM TeamFormation f WHERE f.team.id = :teamId AND f.match IS NULL")
    Optional<TeamFormation> findDefaultByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT f FROM TeamFormation f WHERE f.team.id = :teamId AND f.match.id = :matchId")
    Optional<TeamFormation> findByTeamIdAndMatchId(@Param("teamId") Long teamId, @Param("matchId") Long matchId);

    @Query("SELECT f FROM TeamFormation f WHERE f.match.id = :matchId")
    List<TeamFormation> findByMatchId(@Param("matchId") Long matchId);
}
