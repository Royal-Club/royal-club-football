package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlaceholderTeamRequest {

    private Long groupId; // For group-based rounds
    private Long roundId; // For direct knockout rounds

    @NotBlank(message = "Placeholder name is required")
    private String placeholderName;

    private String sourceRule; // JSON string describing where the team will come from

    private Integer seedPosition; // For knockout rounds
}
