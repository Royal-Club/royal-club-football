package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GoalKeeperPriorityDto {
    private Integer priority;
    private Long playerId;
    private String playerName;
    private String employeeId;
    private Integer previousGoalKeepingTournaments;
    private Boolean wasGoalKeeperInMostRecentTournament;
    private List<String> playAsGkDates;  // Format: dd-MM-yy (e.g., "15-11-25")
}

