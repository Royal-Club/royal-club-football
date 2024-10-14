package com.bjit.royalclub.royalclubfootball.repository.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcChart;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherDetail;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountBalanceSummary;
import com.bjit.royalclub.royalclubfootball.model.account.report.AccountsReport;
import com.bjit.royalclub.royalclubfootball.model.account.report.NatureWiseBalanceSheetReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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


    /**
     * Checks if any AcVoucherDetail exists for a given AcChart.
     *
     * @param acChart the AcChart entity to check for.
     * @return true if any AcVoucherDetail exists for the provided AcChart.
     */
//    @Query("SELECT COUNT(v) > 0 FROM AcVoucherDetail v WHERE v.acChart = :acChart")
    boolean existsByAcChart(AcChart acChart);

    @Query("SELECT SUM(v.cr) FROM AcVoucherDetail v")
    BigDecimal getTotalCredits();  // Sum of all credit entries

    @Query("SELECT SUM(v.dr) FROM AcVoucherDetail v")
    BigDecimal getTotalDebits();   // Sum of all debit entries

    @Query("SELECT FUNCTION('MONTH', v.voucherDate) as month, " +
            "FUNCTION('YEAR', v.voucherDate) as year, " +
            "SUM(vd.cr) as totalCollection, " +
            "SUM(vd.dr) as totalExpense " +
            "FROM AcVoucherDetail vd " +
            "JOIN vd.voucher v " +
            "GROUP BY FUNCTION('MONTH', v.voucherDate), FUNCTION('YEAR', v.voucherDate) " +
            "ORDER BY year, month")
    List<Object[]> getMonthlyCollectionAndExpense();


}
