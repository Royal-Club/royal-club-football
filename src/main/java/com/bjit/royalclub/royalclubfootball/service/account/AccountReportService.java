package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.model.account.report.AccountBalanceSummary;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountSummaryResponse;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountsReport;
import com.bjit.royalclub.royalclubfootball.model.account.report.MonthWiseSummaryResponse;
import com.bjit.royalclub.royalclubfootball.model.account.report.NatureWiseBalanceSheetReport;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountReportService {

    private final AcVoucherDetailRepository acVoucherDetailRepository;

    public List<AccountsReport> getAccountsReport() {
        return acVoucherDetailRepository.generateAccountsReport();
    }

    public List<AccountBalanceSummary> getAccountBalancesSummary() {
        return acVoucherDetailRepository.getAccountBalancesSummary();
    }

    public List<NatureWiseBalanceSheetReport> getBalanceSheetReport() {
        return acVoucherDetailRepository.getBalanceSheetByNatureType();
    }

    public BigDecimal getCurrentBalance() {
        BigDecimal totalCredits = acVoucherDetailRepository.getTotalCredits();  // Collections
        BigDecimal totalDebits = acVoucherDetailRepository.getTotalDebits();    // Expenses

        return totalCredits.subtract(totalDebits);
    }

    public AccountSummaryResponse getAccountSummary() {
        BigDecimal totalCredits = acVoucherDetailRepository.getTotalCredits();  // Total collections (credits)
        BigDecimal totalDebits = acVoucherDetailRepository.getTotalDebits();    // Total expenses (debits)
        BigDecimal currentBalance = totalCredits.subtract(totalDebits);         // Balance = credits - debits

        return AccountSummaryResponse.builder()
                .totalCollection(totalCredits)
                .totalExpense(totalDebits)
                .currentBalance(currentBalance)
                .build();
    }

    public List<MonthWiseSummaryResponse> getMonthlyAccountSummary() {
        List<Object[]> results = acVoucherDetailRepository.getMonthlyCollectionAndExpense();
        List<MonthWiseSummaryResponse> summaries = new ArrayList<>();

        for (Object[] result : results) {
            Integer month = (Integer) result[0];
            Integer year = (Integer) result[1];
            BigDecimal totalCollection = (BigDecimal) result[2];
            BigDecimal totalExpense = (BigDecimal) result[3];
            BigDecimal currentBalance = totalCollection.subtract(totalExpense);

            summaries.add(MonthWiseSummaryResponse.builder()
                    .month(month)
                    .year(year)
                    .totalCollection(totalCollection)
                    .totalExpense(totalExpense)
                    .currentBalance(currentBalance)
                    .build());
        }

        return summaries;
    }

}
