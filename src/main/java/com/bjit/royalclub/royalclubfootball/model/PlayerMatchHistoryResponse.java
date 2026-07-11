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
public class PlayerMatchHistoryResponse {

    private Long matchId;
    private LocalDateTime matchDate;
    private String tournamentName;

    private Long teamId;
    private String teamName;
    private String opponentTeamName;

    private Integer teamScore;
    private Integer opponentScore;
    private String result; // WIN | DRAW | LOSS

    private Integer goalsScored;
    private Integer assists;
    private Integer yellowCards;
    private Integer redCards;
    private Integer minutesPlayed;
}
