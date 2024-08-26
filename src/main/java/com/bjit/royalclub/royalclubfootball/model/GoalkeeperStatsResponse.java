package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoalkeeperStatsResponse {
    private Long playerId;
    private String playerName;
    private Long goalkeeperCount;
}
