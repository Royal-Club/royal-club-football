package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MonthlyCost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MonthlyCostRepository extends JpaRepository<MonthlyCost, Long> {
    List<MonthlyCost> findByMonthOfCostBetween(LocalDate startDate, LocalDate endDate);
}
