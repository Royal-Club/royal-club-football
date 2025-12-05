package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogicNodeRequest {

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "Node name is required")
    private String nodeName;

    @NotNull(message = "Node type is required")
    private String nodeType; // ADVANCEMENT, FILTER, CUSTOM

    private Long sourceRoundId; // Nullable - can be from group instead

    private Long sourceGroupId; // Nullable - can be from round instead

    @NotNull(message = "Target round ID is required")
    private Long targetRoundId;

    /**
     * JSON configuration for the rule
     * Example: {"type": "TOP_N_FROM_EACH", "topN": 2, "tieBreakerRules": {...}}
     */
    private String ruleConfig;

    private Integer priorityOrder;

    private Boolean isActive;

    private Boolean autoExecute;
}

