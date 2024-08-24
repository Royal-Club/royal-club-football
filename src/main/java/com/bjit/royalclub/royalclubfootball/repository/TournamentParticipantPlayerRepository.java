package com.bjit.royalclub.royalclubfootball.repository;


import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipantPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TournamentParticipantPlayerRepository extends JpaRepository<TournamentParticipantPlayer, Long> {

    List<TournamentParticipantPlayer> findAllByTournamentId(Long tournamentId);
}
