package com.bjit.royalclub.royalclubfootball.repository.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcBillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcBillPaymentRepository extends JpaRepository<AcBillPayment, Long> {

    boolean existsByCode(String code);
}
