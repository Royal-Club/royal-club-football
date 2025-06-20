package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.report.AccountSummaryResponse;
import com.bjit.royalclub.royalclubfootball.model.account.report.MonthWiseSummary;
import com.bjit.royalclub.royalclubfootball.model.account.report.MonthWiseSummaryResponse;
import com.bjit.royalclub.royalclubfootball.service.account.AcCollectionService;
import com.bjit.royalclub.royalclubfootball.service.account.AccountReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/reports")
public class AccountReportController {

    private final AccountReportService service;
    private final AcCollectionService acCollectionService;


    @GetMapping("accounts-summary")
    public ResponseEntity<Object> getAccountsReport() {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getAccountsReport());
    }

    @GetMapping("balance-summary")
    public ResponseEntity<Object> getAccountBalancesSummary() {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getAccountBalancesSummary());
    }

    @GetMapping("balance-sheet")
    public ResponseEntity<Object> getBalanceSheet() {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getBalanceSheetReport());
    }

    @GetMapping("/current-balance")
    public ResponseEntity<Object> getCurrentBalance() {
        BigDecimal currentBalance = service.getCurrentBalance();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, currentBalance);
    }

    /**
     * API to get total collection, total expense, and current balance.
     */
    @GetMapping("/summary")
    public ResponseEntity<Object> getAccountSummary() {
        AccountSummaryResponse summaryResponse = service.getAccountSummary();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, summaryResponse);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<Object> getMonthlySummary() {
        List<MonthWiseSummary> summary = service.getMonthlyAccountSummary();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, summary);
    }


    @GetMapping("/monthly-income-expense-summary")
    public ResponseEntity<Object> getMonthlyIncomeAndExpenseSummary(@RequestParam(value = "year", required = false) Integer year) {
        List<MonthWiseSummaryResponse> summary = service.getMonthlyIncomeAndExpenseSummary(year);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, summary);
    }

    @GetMapping("/player-collection-metrics")
    public ResponseEntity<Object> getPlayerCollectionMetrics(@RequestParam(value = "year", required = false) Integer year) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, acCollectionService.getPlayerCollectionMetrics(year));
    }
}
