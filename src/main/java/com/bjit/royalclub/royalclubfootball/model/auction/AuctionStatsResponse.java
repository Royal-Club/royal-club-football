package com.bjit.royalclub.royalclubfootball.model.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionStatsResponse {
    private String mostExpensivePlayerName;
    private Integer mostExpensivePrice;
    private String cheapestSoldPlayerName;
    private Integer cheapestSoldPrice;
    private Integer averageSalePrice;
    private Integer totalMoneySpent;
    private String mostActiveBiddingTeam;
    private Integer highestBidWarCount;
    private String highestBidWarPlayerName;
}
