package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClubRuleRequest {
    @NotBlank(message = "rule description is required.")
    private String description;
}
