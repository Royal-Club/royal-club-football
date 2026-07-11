package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.model.auction.AuctionPlayerRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionPlayerResponse;

import java.util.List;

public interface AuctionPlayerPoolService {
    List<AuctionPlayerResponse> getPlayerPool(Long tournamentId);
    AuctionPlayerResponse addExistingPlayer(Long tournamentId, AuctionPlayerRequest request);
    AuctionPlayerResponse addFromRegistration(Long tournamentId, Long registrationId, AuctionPlayerRequest request);
    AuctionPlayerResponse updatePlayer(Long tournamentId, Long auctionPlayerId, AuctionPlayerRequest request);
    void removePlayer(Long tournamentId, Long auctionPlayerId);
    AuctionPlayerResponse restorePlayer(Long tournamentId, Long auctionPlayerId);
}
