package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentRequest {

    private String tournamentName;
    @NotNull(message = "Tournament date is mandatory")
    private LocalDateTime tournamentDate;
    @NotNull(message = "Venue ID is mandatory")
    private Long venueId;

}
