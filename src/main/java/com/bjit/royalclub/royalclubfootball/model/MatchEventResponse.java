package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEventResponse {

    private Long id;
    private Long matchId;
    private String eventType;
    private Long playerId;
    private String playerName;
    private Long teamId;
    private String teamName;
    private Integer eventTime;
    private String description;
    private Long relatedPlayerId;
    private String relatedPlayerName;
    private String details;
    private LocalDateTime createdDate;

}
