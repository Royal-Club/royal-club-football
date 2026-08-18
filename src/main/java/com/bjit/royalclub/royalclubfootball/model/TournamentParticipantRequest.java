package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentParticipantRequest {
    private Long tournamentParticipantId;
    @NotNull(message = "Match Schedule ID is mandatory")
    private Long tournamentId;
    @NotNull(message = "Player ID is mandatory")
    private Long playerId;
    private String comments;
    /**
     * Yes, No, or null for "clear my answer".
     * <p>
     * Boxed on purpose. As a primitive, Jackson turned the clear-response null into {@code false},
     * so a player who withdrew their answer was silently recorded as not attending while the UI
     * told them it had been cleared.
     */
    private Boolean participationStatus;
}
