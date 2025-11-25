package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRoleAssignmentRequest {
    @NotEmpty(message = "Player role mappings cannot be empty")
    private Map<Long, Set<Long>> playerRoleMappings;
}

