package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchCreateRequest {

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

    private Integer matchOrder;

    private Integer matchDurationMinutes;

    private String groupName;

}

