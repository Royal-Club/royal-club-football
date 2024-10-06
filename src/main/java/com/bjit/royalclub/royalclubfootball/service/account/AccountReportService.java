package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.model.account.report.AccountBalanceSummary;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountsReport;
import com.bjit.royalclub.royalclubfootball.model.account.report.NatureWiseBalanceSheetReport;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
