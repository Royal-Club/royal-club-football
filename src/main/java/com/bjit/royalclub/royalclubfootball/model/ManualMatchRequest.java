package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ManualMatchRequest {

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    private Long roundId;
    private Long groupId;

    @NotNull(message = "Home team ID is required")
    private Long homeTeamId;

    @NotNull(message = "Away team ID is required")
    private Long awayTeamId;

    @NotNull(message = "Match date is required")
    private LocalDateTime matchDate;

    private Long venueId;

    private String matchType; // GROUP_STAGE, KNOCKOUT, SEMI_FINAL, FINAL, THIRD_PLACE, QUALIFIER

    private Integer seriesNumber; // For best-of series

    private String bracketPosition; // For bracket visualization (e.g., "QF1", "SF1")

    private Integer matchDurationMinutes;
}
