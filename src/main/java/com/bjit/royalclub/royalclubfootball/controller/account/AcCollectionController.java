package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.service.account.AcCollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/collections")
public class AcCollectionController {
    private final AcCollectionService service;

    @PostMapping
    public ResponseEntity<Object> saveAcCollection(
            @Valid @RequestBody PaymentCollectionRequest paymentRequest) {
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, service.paymentCollection(paymentRequest));
    }

    @GetMapping
    public ResponseEntity<Object> getAllAcCollections() {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getAllAcCollections());
    }

    @PostMapping("/costs")
    public ResponseEntity<Object> recordCost(@Valid @RequestBody MonthlyCostRequest costRequest) {
        service.recordCost(costRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }
}
