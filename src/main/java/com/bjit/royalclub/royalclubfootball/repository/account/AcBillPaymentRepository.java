package com.bjit.royalclub.royalclubfootball.repository.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcBillPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AcBillPaymentRepository extends JpaRepository<AcBillPayment, Long> {

    boolean existsByCode(String code);

    @Query("SELECT a FROM AcBillPayment a WHERE YEAR(a.paymentDate) = :year")
    Page<AcBillPayment> findByYear(int year, Pageable pageable);

    @Query("SELECT a FROM AcBillPayment a WHERE YEAR(a.paymentDate) = :year AND MONTH(a.paymentDate) = :month")
    Page<AcBillPayment> findByYearAndMonth(int year, int month, Pageable pageable);

}
