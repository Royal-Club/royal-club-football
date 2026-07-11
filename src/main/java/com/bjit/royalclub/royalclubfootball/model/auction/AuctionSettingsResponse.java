package com.bjit.royalclub.royalclubfootball.model.auction;

import com.bjit.royalclub.royalclubfootball.enums.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionSettingsResponse {
    private Long id;
    private Long tournamentId;
    private Integer teamBudget;
    private Integer minSquadSize;
    private Integer maxSquadSize;
    private Integer auctionTimerSeconds;
    private Integer bidIncrement;
    private Boolean unsoldReauctionEnabled;
    private Integer timerExtensionSeconds;
    private Integer extendIfBidWithinLastSeconds;
    private String minRoleRequirements;
    private AuctionStatus auctionStatus;
    private LocalDateTime scheduledStartTime;
}
