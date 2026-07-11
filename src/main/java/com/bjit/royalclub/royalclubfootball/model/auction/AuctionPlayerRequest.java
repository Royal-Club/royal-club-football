package com.bjit.royalclub.royalclubfootball.model.auction;

import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerCategory;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerType;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionPlayerRequest {
    private Long playerId;
    private AuctionPlayerType playerType;
    private AuctionPlayerCategory category;
    @Min(1)
    private Integer basePrice;
    private Integer sequenceOrder;
}
