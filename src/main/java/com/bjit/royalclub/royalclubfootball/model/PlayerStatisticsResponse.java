package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatisticsResponse {

    private Long playerId;
    private String playerName;
    private String position;

    // Aggregated Statistics
    private StatisticsSummary statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsSummary {
        private Integer matchesPlayed;
        private Integer goalsScored;
        private Integer assists;
        private Integer goalsAndAssists;
        private Integer yellowCards;
        private Integer redCards;
    }
}
