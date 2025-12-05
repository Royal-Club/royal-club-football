package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdvancementRuleRequest {

    @NotNull(message = "Source round ID is required")
    private Long sourceRoundId;

    private Long sourceGroupId; // Optional - for group-specific rules

    @NotNull(message = "Target round ID is required")
    private Long targetRoundId;

    @NotBlank(message = "Rule type is required")
    private String ruleType; // TOP_N, WINNER, LOSER, BEST_THIRD_PLACE, ALL_TEAMS

    @NotBlank(message = "Rule configuration is required")
    private String ruleConfig; // JSON string with rule details

    private Integer priorityOrder;
}
