package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AcCollectionService {
    @Transactional
    Long paymentCollection(PaymentCollectionRequest paymentRequest);

    List<PaymentResponse> getAllPayments();

    @Transactional
    void recordCost(MonthlyCostRequest costRequest);
}
