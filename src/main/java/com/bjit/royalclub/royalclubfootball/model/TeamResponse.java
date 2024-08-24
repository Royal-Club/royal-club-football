package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamResponse {
    private Long teamId;
    private String teamName;
    private Long tournamentId;
    private String tournamentName;
}
