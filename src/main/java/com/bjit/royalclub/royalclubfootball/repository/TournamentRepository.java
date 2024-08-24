package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByTournamentDateAfter(LocalDateTime dateTime);

    List<Tournament> findByTournamentDateAfterOrderByTournamentDateAsc(LocalDateTime date, Pageable pageable);
}
