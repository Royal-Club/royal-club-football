package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchStatisticsResponse {

    private Long id;
    private Long matchId;
    private Long playerId;
    private String playerName;
    private Long teamId;
    private String teamName;
    private Integer goalsScored;
    private Integer assists;
    private Integer redCards;
    private Integer yellowCards;
    private Integer substitutionIn;
    private Integer substitutionOut;
    private Integer minutesPlayed;

}
