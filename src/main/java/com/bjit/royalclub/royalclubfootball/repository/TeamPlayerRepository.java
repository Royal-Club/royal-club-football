package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long> {
    @Query("SELECT COUNT(tp) > 0 FROM TeamPlayer tp WHERE tp.team.id IN :teamIds AND tp.player.id = :playerId")
    boolean existsByTeamIdsAndPlayerId(@Param("teamIds") List<Long> teamIds, @Param("playerId") Long playerId);
}
