package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcCollection;
import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcCollectionResponse;
import com.bjit.royalclub.royalclubfootball.model.account.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.account.report.PlayerCollectionMetricsResponse;
import com.bjit.royalclub.royalclubfootball.model.account.report.PlayerCollectionReport;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AcCollectionService {

    Long savePaymentCollection(PaymentCollectionRequest paymentRequest);

    List<AcCollectionResponse> getAllAcCollections();

    Long updatePaymentCollection(Long id, PaymentCollectionRequest paymentRequest);

    AcCollectionResponse getAcCollectionResponse(AcCollection acCollection);

    @Transactional
    void recordCost(MonthlyCostRequest costRequest);

    AcCollectionResponse getAcCollection(Long id);

    void deletePaymentCollection(Long id);

    PlayerCollectionMetricsResponse getPlayerCollectionMetrics();
}
