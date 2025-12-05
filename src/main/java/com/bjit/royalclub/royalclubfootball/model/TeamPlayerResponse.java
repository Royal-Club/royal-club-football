package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamPlayerResponse {

    private Long id;
    private Long teamId;
    private Long playerId;
    private String teamName;
    private String playerName;
    private FootballPosition playingPosition;
    private String teamPlayerRole;
    private Boolean isCaptain;
    private Integer jerseyNumber;
}
