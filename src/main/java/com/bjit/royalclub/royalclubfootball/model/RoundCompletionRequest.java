package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoundCompletionRequest {

    @NotNull(message = "Round ID is required")
    private Long roundId;

    private Boolean autoAdvanceTeams; // Whether to automatically populate next round (default: true)

    private Boolean recalculateStandings; // Whether to recalculate group standings (default: true)

    /**
     * Manual team selection for advancement
     * If provided, these teams will be advanced instead of using automatic rules
     * Format: List of team IDs to advance to next round
     */
    private List<Long> selectedTeamIds;
}
