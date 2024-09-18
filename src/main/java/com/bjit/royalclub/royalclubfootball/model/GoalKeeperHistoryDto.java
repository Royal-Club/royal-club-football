package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GoalKeeperHistoryDto {
    private Long playerId;
    private String playerName;
    private LocalDateTime playedDate;
}
