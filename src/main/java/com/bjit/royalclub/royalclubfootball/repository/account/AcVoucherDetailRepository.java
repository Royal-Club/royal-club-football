package com.bjit.royalclub.royalclubfootball.repository.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherDetail;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountBalanceSummary;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountsReport;
import com.bjit.royalclub.royalclubfootball.model.account.report.NatureWiseBalanceSheetReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcVoucherDetailRepository extends JpaRepository<AcVoucherDetail, Long> {

    @Query("SELECT new com.bjit.royalclub.royalclubfootball.model.account.report.AccountsReport(" +
            "v.acChart.acNature.type, " +
            "v.acChart.id, v.acChart.code, v.acChart.name, " +
            "SUM(COALESCE(v.dr, 0)), SUM(COALESCE(v.cr, 0)), " +
            "(SUM(COALESCE(v.dr, 0)) - SUM(COALESCE(v.cr, 0)))) " +
            "FROM AcVoucherDetail v " +
            "GROUP BY v.acChart.acNature.type, v.acChart.id, v.acChart.code, v.acChart.name")
    List<AccountsReport> generateAccountsReport();


    @Query("SELECT new com.bjit.royalclub.royalclubfootball.model.account.report.AccountBalanceSummary(" +
            "v.acChart.acNature.name, " +
            "SUM(COALESCE(v.dr, 0)), " +
            "SUM(COALESCE(v.cr, 0)), " +
            "(SUM(COALESCE(v.dr, 0)) - SUM(COALESCE(v.cr, 0)))" +
            ") " +
            "FROM AcVoucherDetail v " +
            "GROUP BY v.acChart.acNature.name")
    List<AccountBalanceSummary> getAccountBalancesSummary();


    @Query("SELECT new com.bjit.royalclub.royalclubfootball.model.account.report.NatureWiseBalanceSheetReport(" +
            "n.type, " +
            "SUM(COALESCE(v.dr, 0)), " +
            "SUM(COALESCE(v.cr, 0)), " +
            "(SUM(COALESCE(v.dr, 0)) - SUM(COALESCE(v.cr, 0)))" +
            ") " +
            "FROM AcVoucherDetail v " +
            "JOIN v.acChart c " +
            "JOIN c.acNature n " +
            "GROUP BY n.type")
    List<NatureWiseBalanceSheetReport> getBalanceSheetByNatureType();

}
