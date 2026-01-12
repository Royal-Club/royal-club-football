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
