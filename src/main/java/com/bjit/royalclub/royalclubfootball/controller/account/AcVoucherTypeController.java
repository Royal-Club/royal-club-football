package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherTypeResponse;
import com.bjit.royalclub.royalclubfootball.service.account.AcVoucherTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/voucher-types")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AcVoucherTypeController {

    private final AcVoucherTypeService service;


    @GetMapping
    public ResponseEntity<Object> getAcVoucherTypes() {
        List<AcVoucherTypeResponse> costTypeResponses = service.getAcVoucherTypes();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, costTypeResponses);
    }

}
