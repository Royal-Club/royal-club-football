package com.bjit.royalclub.royalclubfootball.repository.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucher;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcVoucherDetailRepository extends JpaRepository<AcVoucherDetail, Long> {
}