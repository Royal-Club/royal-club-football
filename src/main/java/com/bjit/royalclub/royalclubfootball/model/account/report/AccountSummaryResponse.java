package com.bjit.royalclub.royalclubfootball.model.account.report;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountSummaryResponse {
    private BigDecimal totalCollection;  // Total credits
    private BigDecimal totalExpense;     // Total debits
    private BigDecimal currentBalance;   // Total collection - Total expense
}
