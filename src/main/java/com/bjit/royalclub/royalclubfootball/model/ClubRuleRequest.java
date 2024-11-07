package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubRuleRequest {
    @NotBlank(message = "rule description is required.")
    private String description;
}
