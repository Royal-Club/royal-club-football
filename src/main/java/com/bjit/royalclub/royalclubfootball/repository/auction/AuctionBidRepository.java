package com.bjit.royalclub.royalclubfootball.repository.auction;

import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionBidRepository extends JpaRepository<AuctionBid, Long> {
    List<AuctionBid> findByAuctionPlayerIdOrderByBidTimeAsc(Long auctionPlayerId);
    List<AuctionBid> findByTournamentIdOrderByBidTimeDesc(Long tournamentId);

    @Query("SELECT ab FROM AuctionBid ab WHERE ab.auctionPlayer.id = :auctionPlayerId ORDER BY ab.bidAmount DESC LIMIT 1")
    Optional<AuctionBid> findHighestBidForPlayer(@Param("auctionPlayerId") Long auctionPlayerId);

    @Query("SELECT ab FROM AuctionBid ab WHERE ab.tournament.id = :tournamentId AND ab.isWinning = true ORDER BY ab.bidTime DESC LIMIT 1")
    Optional<AuctionBid> findLastWinningBid(@Param("tournamentId") Long tournamentId);

    long countByAuctionPlayerId(Long auctionPlayerId);
}
