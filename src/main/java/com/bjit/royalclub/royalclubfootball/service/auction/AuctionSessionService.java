package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.model.auction.*;

import java.util.List;

public interface AuctionSessionService {
    AuctionSessionResponse getSession(Long tournamentId);
    AuctionSessionResponse startAuction(Long tournamentId);
    AuctionSessionResponse pauseAuction(Long tournamentId);
    AuctionSessionResponse resumeAuction(Long tournamentId);
    AuctionSessionResponse endAuction(Long tournamentId);
    AuctionSessionResponse nextPlayer(Long tournamentId);
    AuctionSessionResponse nextPlayerRandom(Long tournamentId);
    AuctionSessionResponse skipPlayer(Long tournamentId);
    AuctionSessionResponse markSold(Long tournamentId);
    AuctionSessionResponse markUnsold(Long tournamentId);
    AuctionSessionResponse undoLastSale(Long tournamentId);
    AuctionSessionResponse restartBidding(Long tournamentId);
    AuctionSessionResponse startUnsoldRound(Long tournamentId);
    BidResponse placeBid(Long tournamentId, BidRequest request, Long bidderUserId);
    List<BidResponse> getBidsForPlayer(Long tournamentId, Long auctionPlayerId);
    List<BidResponse> getAllBids(Long tournamentId);
    AuctionDashboardResponse getDashboard(Long tournamentId);
    AuctionResultResponse getResults(Long tournamentId);
    void notifyTimerExpired(Long tournamentId);
}
