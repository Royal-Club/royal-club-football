package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TeamAssignmentRequest {

    @NotEmpty(message = "Team IDs cannot be empty")
    private List<Long> teamIds;

    private String assignmentType; // MANUAL, RULE_BASED, PLACEHOLDER

    // For group-based assignments
    private Long groupId;

    // For direct knockout assignments
    private Long roundId;
}
