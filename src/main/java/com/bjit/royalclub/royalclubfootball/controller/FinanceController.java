package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentResponse;
import com.bjit.royalclub.royalclubfootball.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finance")
public class FinanceController {
    private final FinanceService financeService;

    @PostMapping("/collection")
    public ResponseEntity<Object> recordCollection(@Valid @RequestBody PaymentCollectionRequest paymentRequest) {
        PaymentResponse paymentResponse = financeService.paymentCollection(paymentRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, paymentResponse);
    }

    @PostMapping("/costs")
    public ResponseEntity<Object> recordCost(@Valid @RequestBody MonthlyCostRequest costRequest) {
        financeService.recordCost(costRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }
}
