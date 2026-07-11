package com.bjit.royalclub.royalclubfootball.repository.auction;

import com.bjit.royalclub.royalclubfootball.entity.auction.TeamBudget;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamBudgetRepository extends JpaRepository<TeamBudget, Long> {
    List<TeamBudget> findByTournamentId(Long tournamentId);
    Optional<TeamBudget> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    Optional<TeamBudget> findByTournamentIdAndOwnerId(Long tournamentId, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tb FROM TeamBudget tb WHERE tb.tournament.id = :tournamentId AND tb.team.id = :teamId")
    Optional<TeamBudget> findByTournamentIdAndTeamIdForUpdate(
            @Param("tournamentId") Long tournamentId, @Param("teamId") Long teamId);
}
