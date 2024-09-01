package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long> {
    @Query("SELECT COUNT(tp) > 0 FROM TeamPlayer tp WHERE tp.team.id IN :teamIds AND tp.player.id = :playerId")
    boolean existsByTeamIdsAndPlayerId(@Param("teamIds") List<Long> teamIds, @Param("playerId") Long playerId);

    @Query("SELECT tp FROM TeamPlayer tp WHERE tp.team.id = :teamId AND tp.player.id = :playerId")
    Optional<TeamPlayer> findByTeamIdAndPlayerId(@Param("teamId") Long teamId, @Param("playerId") Long playerId);

    @Query("SELECT new com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse(p.id, p.name, COUNT(tp)) " +
            "FROM Player p " +
            "LEFT JOIN TeamPlayer tp ON p.id = tp.player.id AND tp.playingPosition = 'GOALKEEPER' " +
            "WHERE p.id IN :playerIds " +
            "GROUP BY p.id, p.name " +
            "ORDER BY COUNT(tp) ASC")
    List<GoalkeeperStatsResponse> findGoalkeeperStatsByPlayerIds(@Param("playerIds") List<Long> playerIds);

}
