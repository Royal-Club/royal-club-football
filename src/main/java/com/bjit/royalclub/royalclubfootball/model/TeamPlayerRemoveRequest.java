package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamPlayerRemoveRequest {
    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Player ID is required")
    private Long playerId;
}
