package com.bjit.royalclub.royalclubfootball.model.auction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionSettingsRequest {
    @NotNull
    @Min(1)
    private Integer teamBudget;
    @NotNull
    @Min(1)
    private Integer minSquadSize;
    @NotNull
    @Min(1)
    private Integer maxSquadSize;
    @NotNull
    @Min(30)
    private Integer auctionTimerSeconds;
    @NotNull
    @Min(1)
    private Integer bidIncrement;
    private Boolean unsoldReauctionEnabled;
    @Min(5)
    private Integer timerExtensionSeconds;
    @Min(5)
    private Integer extendIfBidWithinLastSeconds;
    private String minRoleRequirements;
}
