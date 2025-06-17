package com.bjit.royalclub.royalclubfootball.model.account.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Builder
@NoArgsConstructor
@Data
@AllArgsConstructor
public class PlayerCollectionMetricsResponse {
    private List<PlayerCollectionReport> metrics;
    private List<Integer> years;
}
