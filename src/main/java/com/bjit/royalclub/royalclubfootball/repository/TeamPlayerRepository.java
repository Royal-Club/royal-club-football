package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long> {
    @Query("SELECT COUNT(tp) > 0 FROM TeamPlayer tp WHERE tp.team.id IN :teamIds AND tp.player.id = :playerId")
    boolean existsByTeamIdsAndPlayerId(@Param("teamIds") List<Long> teamIds, @Param("playerId") Long playerId);

    /**
     * All player IDs assigned to any of the given teams. Batch form of
     * {@link #existsByTeamIdsAndPlayerId} for callers checking many players at once.
     */
    @Query("SELECT DISTINCT tp.player.id FROM TeamPlayer tp WHERE tp.team.id IN :teamIds")
    List<Long> findPlayerIdsByTeamIds(@Param("teamIds") List<Long> teamIds);

    @Query("SELECT tp FROM TeamPlayer tp WHERE tp.team.id = :teamId AND tp.player.id = :playerId")
    Optional<TeamPlayer> findByTeamIdAndPlayerId(@Param("teamId") Long teamId, @Param("playerId") Long playerId);

    @Query("SELECT new com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse(p.id, p.name, COUNT(tp)) " +
            "FROM Player p " +
            "LEFT JOIN TeamPlayer tp ON p.id = tp.player.id AND tp.playingPosition = 'GOALKEEPER' " +
            "LEFT JOIN tp.team t " +
            "WHERE p.id IN :playerIds " +
            "AND (t.id IS NULL OR t.id NOT IN :teamIds) " +
            "GROUP BY p.id, p.name " +
            "ORDER BY COUNT(tp) ASC")
    List<GoalkeeperStatsResponse> findGoalkeeperStatsByPlayerIdsExcludingTeams(
            @Param("playerIds") List<Long> playerIds,
            @Param("teamIds") List<Long> teamIds);

    /**
     * Find all captains for a specific tournament team
     */
    @Query("SELECT tp FROM TeamPlayer tp WHERE tp.team.id = :teamId AND tp.teamPlayerRole IN ('CAPTAIN', 'VICE_CAPTAIN') ORDER BY tp.teamPlayerRole ASC")
    List<TeamPlayer> findCaptainsByTeamId(@Param("teamId") Long teamId);

    /**
     * Check if a player is captain of a team
     */
    @Query("SELECT COUNT(tp) > 0 FROM TeamPlayer tp WHERE tp.team.id = :teamId AND tp.player.id = :playerId AND tp.teamPlayerRole = 'CAPTAIN'")
    boolean isCaptainOfTeam(@Param("teamId") Long teamId, @Param("playerId") Long playerId);

    /**
     * Find a captain by team id and player id
     */
    @Query("SELECT tp FROM TeamPlayer tp WHERE tp.team.id = :teamId AND tp.player.id = :playerId AND tp.teamPlayerRole IN ('CAPTAIN', 'VICE_CAPTAIN')")
    Optional<TeamPlayer> findCaptainByTeamIdAndPlayerId(@Param("teamId") Long teamId, @Param("playerId") Long playerId);

    /**
     * Find all team players by team id
     */
    @Query("SELECT tp FROM TeamPlayer tp WHERE tp.team.id = :teamId")
    List<TeamPlayer> findAllByTeamId(@Param("teamId") Long teamId);

    /**
     * The teams a player was put on for one tournament. A player belongs to at most
     * one team per tournament, but this returns a list rather than an Optional so a
     * double assignment surfaces as a stale row to clean up, not a failed request.
     */
    @Query("SELECT tp.team.id FROM TeamPlayer tp "
            + "WHERE tp.team.tournament.id = :tournamentId AND tp.player.id = :playerId "
            + "ORDER BY tp.id")
    List<Long> findTeamIdsByTournamentIdAndPlayerId(@Param("tournamentId") Long tournamentId,
                                                    @Param("playerId") Long playerId);

    /**
     * {playerId, tournamentId} for every team assignment in the given tournaments.
     * Being on a team sheet is what counts as having turned up, so this is the
     * "played" side of the attendance report.
     */
    @Query("SELECT DISTINCT tp.player.id, tp.team.tournament.id FROM TeamPlayer tp "
            + "WHERE tp.team.tournament.id IN :tournamentIds")
    List<Object[]> findPlayerTournamentPairs(@Param("tournamentIds") Collection<Long> tournamentIds);

}
