package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.service.account.AccountReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/reports")
public class AccountReportController {

    private final AccountReportService service;

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

}
