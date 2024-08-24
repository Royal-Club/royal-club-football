package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerParticipationResponse {
    private Long playerId;
    private String playerName;
    private String employeeId;
    private Boolean participationStatus;
}
