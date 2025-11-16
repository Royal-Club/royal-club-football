package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GoalKeeperPriorityDto {
    private Integer priority;
    private Long playerId;
    private String playerName;
    private String employeeId;
    private List<String> playAsGkDates;  // Format: dd-MM-yy (e.g., "15-11-25")
    private Integer totalTournamentParticipations;        // How many tournaments player participated
    private Integer activeTournamentCount;                // Total active tournaments in system
    private Double participationFrequency;                // %: (totalParticipations / activeTournamentCount) * 100
    private Integer totalGoalKeeperTournaments;           // Total times played as goalkeeper
    private LocalDateTime lastGoalKeeperDate;             // Last date played as goalkeeper
    private String lastPlayedTournamentDate;              // Last tournament participation date (excluding current) - Format: dd-MM-yy
}
