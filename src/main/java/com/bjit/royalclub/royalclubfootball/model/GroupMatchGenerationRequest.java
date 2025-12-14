package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.FixtureFormat;
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
public class GroupMatchGenerationRequest {

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "Fixture format is required")
    private FixtureFormat fixtureFormat;

    /**
     * Time gap between matches in minutes
     * Default: 180 (3 hours)
     */
    private Integer matchTimeGapMinutes;

    /**
     * Match duration in minutes
     * Default: 90 minutes
     */
    private Integer matchDurationMinutes;

    /**
     * Venue ID (optional - if all matches in same venue)
     */
    private Long venueId;
}
