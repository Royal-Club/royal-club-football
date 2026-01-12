package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.PrizeCategory;
import com.bjit.royalclub.royalclubfootball.enums.PrizeType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentPrizeRequest {

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "Prize type is required")
    private PrizeType prizeType;

    // One of these must be provided based on prizeType
    private Long teamId;
    private Long playerId;

    @NotNull(message = "Position rank is required")
    @Min(value = 1, message = "Position rank must be at least 1")
    private Integer positionRank;

    private BigDecimal prizeAmount;

    @NotNull(message = "Prize category is required")
    private PrizeCategory prizeCategory;

    private String description;

    private List<String> imageLinks;
}
