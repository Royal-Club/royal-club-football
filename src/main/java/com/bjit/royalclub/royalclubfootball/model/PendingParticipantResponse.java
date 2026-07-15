package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A player who has NOT yet responded (no participant row) for a given tournament — i.e. still PENDING.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingParticipantResponse {
    private Long playerId;
    private String playerName;
    private String employeeId;
    private String email;
    private String mobileNo;
}
