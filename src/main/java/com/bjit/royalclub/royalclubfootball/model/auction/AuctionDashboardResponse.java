package com.bjit.royalclub.royalclubfootball.model.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionDashboardResponse {
    private AuctionSessionResponse session;
    private AuctionPlayerResponse currentPlayer;
    private List<BidResponse> currentPlayerBids;
    private List<TeamBudgetResponse> teamBudgets;
    private List<AuctionPlayerResponse> soldPlayers;
    private List<AuctionPlayerResponse> unsoldPlayers;
    private AuctionStatsResponse statistics;
    private Long totalPlayers;
    private Long soldCount;
    private Long unsoldCount;
    private Long remainingCount;
}
