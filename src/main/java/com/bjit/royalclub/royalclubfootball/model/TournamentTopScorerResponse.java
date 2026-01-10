package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentTopScorerResponse {

    private Long playerId;
    private String playerName;
    private Long teamId;
    private String teamName;
    private String position;
    private Integer goalsScored;
    private Integer assists;
    private Integer matchesPlayed;
}
