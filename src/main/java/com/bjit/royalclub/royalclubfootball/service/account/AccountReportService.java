package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.model.account.report.AccountBalanceSummary;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountSummaryResponse;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountsReport;
import com.bjit.royalclub.royalclubfootball.model.account.report.MonthWiseSummary;
import com.bjit.royalclub.royalclubfootball.model.account.report.MonthWiseSummaryResponse;
import com.bjit.royalclub.royalclubfootball.model.account.report.NatureWiseBalanceSheetReport;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        BigDecimal totalAssets =
                Optional.ofNullable(acVoucherDetailRepository.getTotalAssets()).orElse(BigDecimal.ZERO);// Total assets
        BigDecimal totalExpenses =
                Optional.ofNullable(acVoucherDetailRepository.getTotalExpenses()).orElse(BigDecimal.ZERO);  // Total expenses

        // Calculate current balance as total assets minus total expenses
        BigDecimal currentBalance = totalAssets.subtract(totalExpenses);

        return AccountSummaryResponse.builder()
                .totalCollection(totalAssets)
                .totalExpense(totalExpenses)
                .currentBalance(currentBalance)
                .build();
    }


    public List<MonthWiseSummary> getMonthlyAccountSummary() {
        List<Object[]> results = acVoucherDetailRepository.getMonthlyCollectionAndExpense();
        List<MonthWiseSummary> summaries = new ArrayList<>();

        for (Object[] result : results) {
            Integer month = (Integer) result[0];
            Integer year = (Integer) result[1];
            BigDecimal totalCollection = (BigDecimal) result[2];
            BigDecimal totalExpense = (BigDecimal) result[3];
            BigDecimal currentBalance = totalCollection.subtract(totalExpense);

            summaries.add(MonthWiseSummary.builder()
                    .month(month)
                    .year(year)
                    .totalCollection(totalCollection)
                    .totalExpense(totalExpense)
                    .currentBalance(currentBalance)
                    .build());
        }

        return summaries;
    }

    public List<MonthWiseSummaryResponse> getMonthlyIncomeAndExpenseSummary(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear(); // Set the current year if year parameter is null
        }

        List<Object[]> results = acVoucherDetailRepository.getMonthlyIncomeAndExpenseByYear(year);
        List<MonthWiseSummaryResponse> summaries = new ArrayList<>();
        BigDecimal closingBalance = BigDecimal.ZERO;

        for (Object[] result : results) {
            Integer month = (Integer) result[0];
            Integer yearValue = (Integer) result[1];
            BigDecimal totalIncome = (BigDecimal) result[2];
            BigDecimal totalExpense = (BigDecimal) result[3];

            String monthName = Month.of(month).name(); // Get the month name from the month number
            monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase(); // Capitalize only the first letter

            BigDecimal currentMonthBalance = totalIncome.subtract(totalExpense);
            closingBalance = closingBalance.add(currentMonthBalance);

            summaries.add(MonthWiseSummaryResponse.builder()
                    .month(month)
                    .monthName(monthName)
                    .year(yearValue)
                    .totalIncome(totalIncome)
                    .totalExpense(totalExpense)
                    .currentBalance(currentMonthBalance)
                    .closingBalance(closingBalance)  // Add the closing balance for each month
                    .build());
        }

        return summaries;
    }

}
