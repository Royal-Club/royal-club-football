package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DateRangeSummaryResponse {
    private String startDate;
    private String endDate;
    private double cashOnPreviousMonths;
    private List<MonthlySummaryResponse> monthlySummaries;
}
