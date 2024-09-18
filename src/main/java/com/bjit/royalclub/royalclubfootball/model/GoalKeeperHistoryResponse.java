package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoalKeeperHistoryResponse {
    private int totalRounds;
    private Map<Integer, List<GoalKeeperHistoryDto>> roundData;

}
