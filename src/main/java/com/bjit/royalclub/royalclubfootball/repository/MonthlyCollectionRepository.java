package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MonthlyCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MonthlyCollectionRepository extends JpaRepository<MonthlyCollection, Long> {
    List<MonthlyCollection> findByMonthOfPaymentBetween
            (LocalDate startDate, LocalDate endDate);
}
