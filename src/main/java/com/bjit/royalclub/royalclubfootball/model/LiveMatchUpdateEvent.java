package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveMatchUpdateEvent {

    private Long tournamentId;
    private Long matchId;
    private String eventType;
    private Long timestamp;
}
