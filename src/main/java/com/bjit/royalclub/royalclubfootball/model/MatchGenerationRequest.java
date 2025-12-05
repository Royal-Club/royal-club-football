package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchGenerationRequest {

    private Long groupId; // For group-based match generation
    private Long roundId; // For direct knockout match generation

    @NotBlank(message = "Generation method is required")
    private String method; // ROUND_ROBIN_SINGLE, ROUND_ROBIN_DOUBLE, KNOCKOUT_BRACKET

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @Min(value = 1, message = "Time gap must be at least 1 minute")
    private Integer timeGapMinutes; // Time between matches (default: 180)

    private Integer matchDurationMinutes; // Match length (default: 90)

    private Long defaultVenueId; // Default venue for all matches

    private Integer encountersPerPairing; // For CUSTOM_MULTIPLE format
}
