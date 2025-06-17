package com.bjit.royalclub.royalclubfootball.repository.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AcCollectionRepository extends JpaRepository<AcCollection, Long> {
    List<AcCollection> findByMonthOfPaymentBetween
            (LocalDate startDate, LocalDate endDate);

    AcCollection findByTransactionId(String transactionId);

    @Query("SELECT DISTINCT YEAR(c.date) FROM AcCollection c ORDER BY YEAR(c.date) DESC")
    List<Integer> findAllCollectionYears();
}
