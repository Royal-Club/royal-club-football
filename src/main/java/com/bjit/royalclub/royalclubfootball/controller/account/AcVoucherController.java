package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.service.account.AcVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/vouchers")
//@PreAuthorize("hasAnyRole('ADMIN')")
public class AcVoucherController {
    private final AcVoucherService service;

    @GetMapping
    public ResponseEntity<Object> getAllAcVouchers(
            @RequestParam(value = "isDetailsResponse",
                    required = false)
            Boolean isDetailsResponse
    ) {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getAllAcVouchers(isDetailsResponse));
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> getAllAcVouchers(@PathVariable Long id,
                                                   @RequestParam(value = "isDetailsResponse",
                                                           required = false)
                                                   Boolean isDetailsResponse
                                                   ) {
        return buildSuccessResponse(
                HttpStatus.OK, FETCH_OK, service.getAcVoucherResponse(id,
                        isDetailsResponse));
    }

}
