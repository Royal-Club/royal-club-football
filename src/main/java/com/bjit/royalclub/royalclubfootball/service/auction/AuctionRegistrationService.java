package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerCategory;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionRegistrationResponse;

import java.util.List;

public interface AuctionRegistrationService {
    AuctionRegistrationResponse register(AuctionRegistrationRequest request);
    AuctionRegistrationResponse quickRegisterExistingPlayer(Long tournamentId);
    List<AuctionRegistrationResponse> getRegistrations(Long tournamentId, String status);
    AuctionRegistrationResponse getRegistration(Long id);
    AuctionRegistrationResponse approve(Long id);
    AuctionRegistrationResponse reject(Long id, String reason);
    AuctionRegistrationResponse undoReject(Long id);
    AuctionRegistrationResponse approveAndAddToPool(Long id, AuctionPlayerCategory category, Integer basePrice);
}
