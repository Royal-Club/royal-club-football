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
public class TeamSquadResponse {
    private Long teamId;
    private String teamName;
    private String ownerName;
    private Integer totalBudget;
    private Integer totalSpent;
    private Integer remainingBudget;
    private Integer playerCount;
    private List<AuctionPlayerResponse> players;
}
