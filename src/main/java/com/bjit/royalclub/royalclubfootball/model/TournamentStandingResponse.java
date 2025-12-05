package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentStandingResponse {

    private Long teamId;
    private String teamName;
    private Integer points;
    private Integer goalsFor;
    private Integer goalsAgainst;
    private Integer matches;
    private Integer wins;
    private Integer draws;
    private Integer losses;
    private Integer goalDifference;

}
