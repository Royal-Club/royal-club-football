package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TournamentTeamResponse {
    private Long teamId;
    private String teamName;
    private List<TeamPlayerResponse> players;
}
