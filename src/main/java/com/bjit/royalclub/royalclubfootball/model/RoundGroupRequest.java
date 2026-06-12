package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoundGroupRequest {

    @NotNull(message = "Round ID is required")
    private Long roundId;

    private Long parentGroupId;

    @NotBlank(message = "Group name is required")
    private String groupName;

    private String groupFormat; // MANUAL, ROUND_ROBIN_SINGLE, ROUND_ROBIN_DOUBLE, CUSTOM_MULTIPLE

    private String advancementRule; // JSON string

    @Min(value = 2, message = "Max teams must be at least 2")
    private Integer maxTeams;
}
