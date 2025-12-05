package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentRoundRequest {

    @NotNull(message = "Tournament ID is required")
    private Long tournamentId;

    @NotNull(message = "Round number is required")
    @Min(value = 1, message = "Round number must be at least 1")
    private Integer roundNumber;

    @NotBlank(message = "Round name is required")
    private String roundName;

    @NotBlank(message = "Round type is required")
    private String roundType; // GROUP_BASED, DIRECT_KNOCKOUT

    private String roundFormat; // ROUND_ROBIN, SINGLE_ELIMINATION, DOUBLE_ELIMINATION, SWISS_SYSTEM, CUSTOM

    private String advancementRule; // JSON string

    @NotNull(message = "Sequence order is required")
    @Min(value = 1, message = "Sequence order must be at least 1")
    private Integer sequenceOrder;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
}
