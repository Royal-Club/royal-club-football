package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamPlayerRequest {

    private long id;

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Playing position is required")
    private String playingPosition;
}
