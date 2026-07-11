package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentUpdateRequest {
    private Long id;
    private String tournamentName;
    private String title;
    private String season;
    private String description;
    private String rules;
    private String roadmapImageUrl;
    @NotNull(message = "Tournament date is mandatory")
    private LocalDateTime tournamentDate;
    @NotNull(message = "Venue ID is mandatory")
    private Long venueId;

    // Fixture system fields
    private String sportType;
    private String tournamentType;
    private Integer groupCount;

    // Auction
    private boolean auctionMode;

    // Viewer default selection field
    private Boolean defaultTournament;
}
