package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {

    List<TournamentParticipant> findAllByTournamentIdAndParticipationStatusTrue(Long tournamentId);

    @Query("SELECT COUNT(tp) > 0 FROM TournamentParticipant tp WHERE tp.tournament.id = :tournamentId AND tp.player.id = :playerId")
    boolean existsByTournamentIdAndPlayerId(@Param("tournamentId") Long tournamentId, @Param("playerId") Long playerId);
}
