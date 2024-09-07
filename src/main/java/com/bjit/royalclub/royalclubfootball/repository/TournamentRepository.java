package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TournamentRepository extends JpaRepository<Tournament, Long>, JpaSpecificationExecutor<Tournament> {
    @Modifying
    @Query("UPDATE Tournament t SET t.isActive = false, t.tournamentStatus = 'CONCLUDED' " +
            "WHERE t.tournamentDate < CURRENT_TIMESTAMP AND t.isActive = true")
    void deactivateAndConcludePastTournaments();

}
