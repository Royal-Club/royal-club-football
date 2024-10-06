package com.bjit.royalclub.royalclubfootball.model.account.report;

import com.bjit.royalclub.royalclubfootball.enums.AcNatureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountsReport {
    private AcNatureType accountType;
    private Long accountId;
    private String accountCode;
    private String accountName;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal balance;

}
