package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GoalKeeperQueueResponseDto {
    private Long tournamentId;
    private String tournamentName;
    private LocalDateTime tournamentDate;
    private List<GoalKeeperPriorityDto> goalKeeperPriorityQueue;
}

