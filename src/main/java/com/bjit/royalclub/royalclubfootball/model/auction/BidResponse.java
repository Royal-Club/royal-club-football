package com.bjit.royalclub.royalclubfootball.model.auction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private Long id;
    private Long auctionPlayerId;
    private Long teamId;
    private String teamName;
    private Long bidderUserId;
    private String bidderName;
    private Integer bidAmount;
    private LocalDateTime bidTime;
    private Boolean isWinning;
}
