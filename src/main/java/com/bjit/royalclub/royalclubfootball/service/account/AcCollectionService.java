package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcCollection;
import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.AcCollectionResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface AcCollectionService {
    @Transactional
    Long paymentCollection(PaymentCollectionRequest paymentRequest);

    List<AcCollectionResponse> getAllAcCollections();

    AcCollectionResponse getAcCollectionResponse(AcCollection acCollection);

    @Transactional
    void recordCost(MonthlyCostRequest costRequest);
}
