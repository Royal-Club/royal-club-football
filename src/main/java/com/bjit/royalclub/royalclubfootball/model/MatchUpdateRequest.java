package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchUpdateRequest {

    @NotNull(message = "Match status is mandatory")
    private String matchStatus;

    @PositiveOrZero(message = "Home team score must be zero or positive")
    private Integer homeTeamScore;

    @PositiveOrZero(message = "Away team score must be zero or positive")
    private Integer awayTeamScore;

    @PositiveOrZero(message = "Elapsed time must be zero or positive")
    private Integer elapsedTimeSeconds;

}
