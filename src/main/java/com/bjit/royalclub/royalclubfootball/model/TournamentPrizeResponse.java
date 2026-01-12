package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.PrizeCategory;
import com.bjit.royalclub.royalclubfootball.enums.PrizeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TournamentPrizeResponse {
    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private PrizeType prizeType;

    // Team info (if team prize)
    private Long teamId;
    private String teamName;

    // Player info (if player prize)
    private Long playerId;
    private String playerName;
    private String playerEmployeeId;

    private Integer positionRank;
    private BigDecimal prizeAmount;
    private PrizeCategory prizeCategory;
    private String description;
    private List<String> imageLinks;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
