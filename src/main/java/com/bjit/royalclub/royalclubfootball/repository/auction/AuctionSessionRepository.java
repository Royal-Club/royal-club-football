package com.bjit.royalclub.royalclubfootball.repository.auction;

import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionSession;
import com.bjit.royalclub.royalclubfootball.enums.AuctionSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionSessionRepository extends JpaRepository<AuctionSession, Long> {
    Optional<AuctionSession> findByTournamentId(Long tournamentId);
    Optional<AuctionSession> findByTournamentIdAndStatus(Long tournamentId, AuctionSessionStatus status);
    List<AuctionSession> findAllByStatusAndCurrentTimerEndsAtIsNotNull(AuctionSessionStatus status);
}
