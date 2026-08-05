package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A full line-up save. The slot list replaces whatever was stored before, so
 * the client always sends the complete sheet — starters first, then the bench
 * in the order the captain arranged it.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamFormationRequest {

    @NotBlank(message = "Preset name is required")
    @Size(max = 30, message = "Preset name cannot exceed 30 characters")
    private String presetName;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @Valid
    @NotEmpty(message = "A formation needs at least one slot")
    private List<TeamFormationSlotRequest> slots;
}
