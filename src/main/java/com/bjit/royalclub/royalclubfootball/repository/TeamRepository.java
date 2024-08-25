package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    @Query("select t from Team t left join fetch t.teamPlayers tp where t.tournament.id = :tournamentId")
    List<Team> findTeamsWithPlayersByTournamentId(@Param("tournamentId") Long tournamentId);
}
