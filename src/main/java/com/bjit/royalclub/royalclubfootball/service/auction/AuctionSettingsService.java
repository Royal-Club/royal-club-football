package com.bjit.royalclub.royalclubfootball.service.auction;

import com.bjit.royalclub.royalclubfootball.model.auction.AuctionSettingsRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionSettingsResponse;

public interface AuctionSettingsService {
    AuctionSettingsResponse getSettings(Long tournamentId);
    AuctionSettingsResponse createSettings(Long tournamentId, AuctionSettingsRequest request);
    AuctionSettingsResponse updateSettings(Long tournamentId, AuctionSettingsRequest request);
}
