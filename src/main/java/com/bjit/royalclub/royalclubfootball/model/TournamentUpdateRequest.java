package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    /** Players per side including the keeper. Left unchanged when omitted. */
    @Min(value = 4, message = "Team size must be at least 4")
    @Max(value = 11, message = "Team size cannot exceed 11")
    private Integer teamSize;

    // Auction
    private boolean auctionMode;

    // Viewer default selection field
    private Boolean defaultTournament;
}
