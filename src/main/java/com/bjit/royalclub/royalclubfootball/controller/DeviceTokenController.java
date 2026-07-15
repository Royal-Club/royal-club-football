package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.DeviceTokenRequest;
import com.bjit.royalclub.royalclubfootball.service.DeviceTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    @PostMapping
    public ResponseEntity<Object> registerToken(@Valid @RequestBody DeviceTokenRequest request) {
        deviceTokenService.registerToken(request);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }

    @DeleteMapping
    public ResponseEntity<Object> unregisterToken(@RequestParam @NotBlank String token) {
        deviceTokenService.unregisterToken(token);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }
}
