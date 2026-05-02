package com.bjit.royalclub.royalclubfootball.repository.auction;

import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionPlayer;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionPlayerRepository extends JpaRepository<AuctionPlayer, Long> {
    List<AuctionPlayer> findByTournamentIdOrderBySequenceOrderAsc(Long tournamentId);
    List<AuctionPlayer> findByTournamentIdAndStatus(Long tournamentId, AuctionPlayerStatus status);
    boolean existsByTournamentIdAndPlayerId(Long tournamentId, Long playerId);
    boolean existsByTournamentIdAndPlayerIdAndStatusNot(Long tournamentId, Long playerId, AuctionPlayerStatus status);
    long countByTournamentIdAndStatus(Long tournamentId, AuctionPlayerStatus status);
    long countBySoldToTeamId(Long teamId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ap FROM AuctionPlayer ap WHERE ap.id = :id")
    Optional<AuctionPlayer> findByIdForUpdate(@Param("id") Long id);

    Optional<AuctionPlayer> findFirstByTournamentIdAndStatusOrderBySequenceOrderAsc(
            Long tournamentId, AuctionPlayerStatus status);
}
