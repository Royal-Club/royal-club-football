package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchParticipantRequest {
    private Long id;
    @NotNull(message = "Match Schedule ID is mandatory")
    private Long matchScheduleId;
    @NotNull(message = "Player ID is mandatory")
    private Long playerId;
    private boolean participationStatus;
    private boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
