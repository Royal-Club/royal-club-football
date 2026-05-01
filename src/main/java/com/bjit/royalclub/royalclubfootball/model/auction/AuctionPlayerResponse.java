package com.bjit.royalclub.royalclubfootball.model.auction;

import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerCategory;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerStatus;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerType;
import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionPlayerResponse {
    private Long id;
    private Long tournamentId;
    private Long playerId;
    private String playerName;
    private String playerEmail;
    private FootballPosition playingPosition;
    private AuctionPlayerType playerType;
    private AuctionPlayerCategory category;
    private Integer basePrice;
    private Integer currentBid;
    private Long currentHighestTeamId;
    private String currentHighestTeamName;
    private Long soldToTeamId;
    private String soldToTeamName;
    private Integer finalPrice;
    private AuctionPlayerStatus status;
    private Integer auctionRound;
    private BigDecimal playerRating;
    private Integer sequenceOrder;
}
