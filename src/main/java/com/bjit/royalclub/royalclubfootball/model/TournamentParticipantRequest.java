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
    private Long id;
    @NotNull(message = "Match Schedule ID is mandatory")
    private Long tournamentId;
    @NotNull(message = "Player ID is mandatory")
    private Long playerId;
    private String comments;
    private boolean participationStatus;
}
