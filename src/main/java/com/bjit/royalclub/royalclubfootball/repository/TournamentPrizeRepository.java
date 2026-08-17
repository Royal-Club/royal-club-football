package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TournamentPrize;
import com.bjit.royalclub.royalclubfootball.enums.PrizeCategory;
import com.bjit.royalclub.royalclubfootball.enums.PrizeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TournamentPrizeRepository extends JpaRepository<TournamentPrize, Long> {

    @Query("SELECT tp FROM TournamentPrize tp WHERE tp.tournament.id = :tournamentId ORDER BY tp.positionRank ASC")
    List<TournamentPrize> findByTournamentIdOrderByPositionRankAsc(@Param("tournamentId") Long tournamentId);

    @Query("SELECT tp FROM TournamentPrize tp WHERE tp.tournament.id = :tournamentId AND tp.prizeType = :prizeType ORDER BY tp.positionRank ASC")
    List<TournamentPrize> findByTournamentIdAndPrizeTypeOrderByPositionRankAsc(
            @Param("tournamentId") Long tournamentId,
            @Param("prizeType") PrizeType prizeType
    );

    @Query("SELECT tp FROM TournamentPrize tp WHERE tp.tournament.id = :tournamentId AND tp.team.id = :teamId")
    List<TournamentPrize> findByTournamentIdAndTeamId(
            @Param("tournamentId") Long tournamentId,
            @Param("teamId") Long teamId
    );

    @Query("SELECT tp FROM TournamentPrize tp WHERE tp.tournament.id = :tournamentId AND tp.player.id = :playerId")
    List<TournamentPrize> findByTournamentIdAndPlayerId(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId
    );

    /**
     * Everything a player has ever won, newest tournament first - the honours board.
     * <p>
     * Every other query here is scoped to one tournament, which answers "who won this competition"
     * but never "what has this member won". Building the latter from the former would mean one call
     * per tournament the club has ever run.
     */
    @Query("SELECT tp FROM TournamentPrize tp JOIN FETCH tp.tournament t "
            + "WHERE tp.player.id = :playerId ORDER BY t.tournamentDate DESC")
    List<TournamentPrize> findAllByPlayerId(@Param("playerId") Long playerId);

    /** Every prize a team has ever won, newest first. */
    @Query("SELECT tp FROM TournamentPrize tp JOIN FETCH tp.tournament t "
            + "WHERE tp.team.id = :teamId ORDER BY t.tournamentDate DESC")
    List<TournamentPrize> findAllByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT tp FROM TournamentPrize tp WHERE tp.tournament.id = :tournamentId AND tp.team.id = :teamId AND tp.prizeCategory = :prizeCategory")
    Optional<TournamentPrize> findByTournamentIdAndTeamIdAndPrizeCategory(
            @Param("tournamentId") Long tournamentId,
            @Param("teamId") Long teamId,
            @Param("prizeCategory") PrizeCategory prizeCategory
    );

    @Query("SELECT tp FROM TournamentPrize tp WHERE tp.tournament.id = :tournamentId AND tp.player.id = :playerId AND tp.prizeCategory = :prizeCategory")
    Optional<TournamentPrize> findByTournamentIdAndPlayerIdAndPrizeCategory(
            @Param("tournamentId") Long tournamentId,
            @Param("playerId") Long playerId,
            @Param("prizeCategory") PrizeCategory prizeCategory
    );
}
