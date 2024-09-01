package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByTournamentDateAfter(LocalDateTime dateTime);

    List<Tournament> findByTournamentDateAfterOrderByTournamentDateAsc(LocalDateTime date, Pageable pageable);

    @Modifying
    @Query("UPDATE Tournament t SET t.isActive = false WHERE t.tournamentDate < CURRENT_TIMESTAMP AND t.isActive = true")
    void deactivatePastTournaments();
}
