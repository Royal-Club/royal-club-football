package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {

    List<TournamentParticipant> findAllByTournamentIdAndParticipationStatusTrue(Long tournamentId);

    @Query("SELECT tp FROM TournamentParticipant tp JOIN FETCH tp.player WHERE tp.tournament.id = :tournamentId AND tp.participationStatus = true")
    List<TournamentParticipant> findAllByTournamentIdAndParticipationStatusTrueWithPlayer(@Param("tournamentId") Long tournamentId);

    @Query("SELECT COUNT(tp) > 0 FROM TournamentParticipant tp WHERE tp.tournament.id = :tournamentId AND tp.player.id = :playerId")
    boolean existsByTournamentIdAndPlayerId(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    @Query("SELECT COUNT(tp) > 0 FROM TournamentParticipant tp " + "WHERE tp.tournament.id = :tournamentId "
            + "AND tp.player.id = :playerId " + "AND tp.participationStatus = true")
    boolean existsByTournamentIdAndPlayerIdAndParticipationStatusTrue(
            @Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);

    int countByTournamentIdAndParticipationStatusTrue(Long tournamentId);

    @Query("SELECT MAX(t.tournamentDate) FROM TournamentParticipant tp " +
            "JOIN tp.tournament t " +
            "WHERE tp.player.id = :playerId AND tp.participationStatus = true " +
            "AND tp.tournament.id != :currentTournamentId " +
            "AND t.tournamentDate < (SELECT td.tournamentDate FROM Tournament td WHERE td.id = :currentTournamentId)")
    Optional<LocalDateTime> findMostRecentParticipationDateExcludingCurrent(
            @Param("playerId") Long playerId,
            @Param("currentTournamentId") Long currentTournamentId);

    @Query(value = "SELECT COUNT(*) FROM tournament t " +
            "WHERE t.id != :currentTournamentId " +
            "AND t.tournament_date < (SELECT tournament_date FROM tournament WHERE id = :currentTournamentId) " +
            "AND NOT EXISTS (SELECT 1 FROM tournament_participant tp WHERE tp.tournament_id = t.id " +
            "AND tp.player_id = :playerId AND tp.participation_status = true) " +
            "AND t.tournament_date > (SELECT COALESCE(MAX(t2.tournament_date), DATE_SUB(NOW(), INTERVAL 1000 DAY)) " +
            "FROM tournament t2 WHERE EXISTS (SELECT 1 FROM tournament_participant tp2 WHERE tp2.tournament_id = t2.id " +
            "AND tp2.player_id = :playerId AND tp2.participation_status = true) " +
            "AND t2.tournament_date < (SELECT tournament_date FROM tournament WHERE id = :currentTournamentId))",
            nativeQuery = true)
    Integer countConsecutiveMissedTournamentsBeforeCurrent(
            @Param("playerId") Long playerId,
            @Param("currentTournamentId") Long currentTournamentId);

    /**
     * Batch form of {@link #countConsecutiveMissedTournamentsBeforeCurrent}: counts the run of
     * tournaments each player missed immediately before the current one.
     * Returns Object[] of {playerId, count}. Players with a zero-length run produce no row.
     */
    @Query(value = "SELECT p.id, COUNT(t.id) FROM players p " +
            "JOIN tournament t " +
            "ON t.id != :currentTournamentId " +
            "AND t.tournament_date < (SELECT tournament_date FROM tournament WHERE id = :currentTournamentId) " +
            "AND NOT EXISTS (SELECT 1 FROM tournament_participant tp WHERE tp.tournament_id = t.id " +
            "AND tp.player_id = p.id AND tp.participation_status = true) " +
            "AND t.tournament_date > (SELECT COALESCE(MAX(t2.tournament_date), DATE_SUB(NOW(), INTERVAL 1000 DAY)) " +
            "FROM tournament t2 WHERE EXISTS (SELECT 1 FROM tournament_participant tp2 WHERE tp2.tournament_id = t2.id " +
            "AND tp2.player_id = p.id AND tp2.participation_status = true) " +
            "AND t2.tournament_date < (SELECT tournament_date FROM tournament WHERE id = :currentTournamentId)) " +
            "WHERE p.id IN (:playerIds) " +
            "GROUP BY p.id",
            nativeQuery = true)
    List<Object[]> countConsecutiveMissedTournamentsBatch(
            @Param("playerIds") Collection<Long> playerIds,
            @Param("currentTournamentId") Long currentTournamentId);

    /**
     * Every RSVP recorded for the given tournaments as {playerId, tournamentId,
     * participationStatus}. Feeds the club-wide attendance report, which needs
     * the raw responses to work out streaks in order.
     */
    @Query("SELECT tp.player.id, tp.tournament.id, tp.participationStatus FROM TournamentParticipant tp "
            + "WHERE tp.tournament.id IN :tournamentIds")
    List<Object[]> findResponsesByTournamentIds(@Param("tournamentIds") Collection<Long> tournamentIds);

    @Query("SELECT tp.player.id, MAX(t.tournamentDate) FROM TournamentParticipant tp " +
            "JOIN tp.tournament t " +
            "WHERE tp.player.id IN :playerIds AND tp.participationStatus = true " +
            "AND tp.tournament.id != :currentTournamentId " +
            "AND t.tournamentDate < (SELECT td.tournamentDate FROM Tournament td WHERE td.id = :currentTournamentId) " +
            "GROUP BY tp.player.id")
    List<Object[]> findMostRecentParticipationDateBatch(
            @Param("playerIds") Collection<Long> playerIds,
            @Param("currentTournamentId") Long currentTournamentId);

}
