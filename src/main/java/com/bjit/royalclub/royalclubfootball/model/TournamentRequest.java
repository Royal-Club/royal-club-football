package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentRequest {

    private String tournamentName;
    @NotNull(message = "Tournament date is mandatory")
    @FutureOrPresent(message = "Tournament date cannot be in the past")
    private LocalDateTime tournamentDate;
    @NotNull(message = "Venue ID is mandatory")
    private Long venueId;

    // Fixture system fields
    private String sportType;
    private String tournamentType;
    private Integer groupCount;

}
