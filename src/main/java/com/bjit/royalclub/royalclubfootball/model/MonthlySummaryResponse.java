package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlySummaryResponse {
    private String month;  // Format: YYYY-MM
    private double totalCollection;
    private double totalCost;
    private Map<String, Double> costTypeWiseCost;
    private double cashOnHand;
}
