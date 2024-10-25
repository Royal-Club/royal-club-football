package com.bjit.royalclub.royalclubfootball.model.account.report;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthWiseSummary {
    private Integer month;
    private Integer year;
    private BigDecimal totalCollection;
    private BigDecimal totalExpense;
    private BigDecimal currentBalance;
}
