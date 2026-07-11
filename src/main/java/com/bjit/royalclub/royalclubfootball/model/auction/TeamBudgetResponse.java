package com.bjit.royalclub.royalclubfootball.model.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamBudgetResponse {
    private Long id;
    private Long tournamentId;
    private Long teamId;
    private String teamName;
    private Long ownerId;
    private String ownerName;
    private Integer totalBudget;
    private Integer remainingBudget;
    private Integer totalSpent;
    private Integer playersBought;
}
