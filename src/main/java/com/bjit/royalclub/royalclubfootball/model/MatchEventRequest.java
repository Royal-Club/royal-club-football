package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.MatchEventType;
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
public class MatchEventRequest {

    @NotNull(message = "Match ID is mandatory")
    private Long matchId;

    @NotNull(message = "Event type is mandatory")
    private MatchEventType eventType;

    @NotNull(message = "Player ID is mandatory")
    private Long playerId;

    @NotNull(message = "Team ID is mandatory")
    private Long teamId;

    @NotNull(message = "Event time is mandatory")
    @PositiveOrZero(message = "Event time must be zero or positive")
    private Integer eventTime;

    private String description;

    private Long relatedPlayerId;

    private String details;

}
