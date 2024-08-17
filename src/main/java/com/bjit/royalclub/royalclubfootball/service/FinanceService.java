package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentResponse;
import jakarta.transaction.Transactional;

public interface FinanceService {
    @Transactional
    PaymentResponse recordCollection(PaymentCollectionRequest paymentRequest);
}
