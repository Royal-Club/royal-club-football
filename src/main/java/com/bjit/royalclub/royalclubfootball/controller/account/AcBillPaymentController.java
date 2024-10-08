package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcBillPaymentRequest;
import com.bjit.royalclub.royalclubfootball.service.account.AcBillPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("ac/bill-payments")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AcBillPaymentController {
    private final AcBillPaymentService service;

    @PostMapping
    public ResponseEntity<Object> saveAcBillPayment(
            @Valid @RequestBody AcBillPaymentRequest paymentRequest) {
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, service.save(paymentRequest));
    }

    @GetMapping
    public ResponseEntity<Object> getAllAcBillPayments() {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getAllBillPayments());
    }

}
