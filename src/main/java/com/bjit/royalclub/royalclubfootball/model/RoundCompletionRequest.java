package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoundCompletionRequest {

    @NotNull(message = "Round ID is required")
    private Long roundId;

    private Boolean recalculateStandings; // Whether to recalculate group standings (default: true)

    /**
     * Manual team selection for advancement
     * Teams to advance to next round (optional - if not provided, round is completed without advancement)
     * Format: List of team IDs to advance to next round
     */
    private List<Long> selectedTeamIds;
}
