package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcNatureResponse;
import com.bjit.royalclub.royalclubfootball.service.account.AcNatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/natures")
public class AcNatureController {

    private final AcNatureService service;


    @GetMapping
    public ResponseEntity<Object> getAcVoucherTypes() {
        List<AcNatureResponse> costTypeResponses = service.getAcNatures();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, costTypeResponses);
    }

}