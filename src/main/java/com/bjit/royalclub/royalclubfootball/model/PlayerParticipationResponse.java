package com.bjit.royalclub.royalclubfootball.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PlayerParticipationResponse {
    private Long playerId;
    private String playerName;
    private String employeeId;
    private Boolean participationStatus;
    private String comments;
    private Long tournamentParticipantId;
}
