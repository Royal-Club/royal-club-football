package com.bjit.royalclub.royalclubfootball.model.account.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthWiseSummaryResponse {
    private Integer month;
    private String monthName;
    private Integer year;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal currentBalance; // Balance for the specific month
    private BigDecimal closingBalance; // Closing balance considering the running balance
}