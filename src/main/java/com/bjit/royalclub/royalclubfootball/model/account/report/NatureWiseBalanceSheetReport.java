package com.bjit.royalclub.royalclubfootball.model.account.report;

import com.bjit.royalclub.royalclubfootball.enums.AcNatureType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NatureWiseBalanceSheetReport {
    private AcNatureType natureType;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal balance;
}
