package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixtureGenerationRequest {

    @NotNull(message = "Tournament ID is mandatory")
    private Long tournamentId;

    // Simplified fixture generation fields
    private List<Long> teamIds;  // Team IDs to create fixtures for
    private List<LocalDateTime> matchDates;  // List of match dates/times
    private Integer timeGapMinutes;  // Time gap between matches in minutes
    private Integer matchDurationMinutes;  // Duration of each match in minutes (e.g., 90 for 90-minute match)

    // Legacy fields (kept for backward compatibility if needed)
    private LocalDateTime startDate;

    private Integer matchesPerDay;

    private Integer daysRest;

    private Long venueId;

    private Integer maxTeamsPerGroup;

    // For custom fixtures: list of match pairings
    // Format: [{"homeTeamId": 1, "awayTeamId": 2}, ...]
    private List<CustomMatchPairing> customMatchPairings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomMatchPairing {
        private Long homeTeamId;
        private Long awayTeamId;
        private Integer round;
        private String groupName;
    }

}
