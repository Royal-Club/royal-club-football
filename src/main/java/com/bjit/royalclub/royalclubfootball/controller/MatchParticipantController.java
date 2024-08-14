package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.MatchParticipantRequest;
import com.bjit.royalclub.royalclubfootball.service.MatchParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.getSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping()
public class MatchParticipantController {
    private final MatchParticipantService matchParticipant;

    @PostMapping
    public ResponseEntity<Object> createMatchParticipant(@Valid @RequestBody MatchParticipantRequest matchParticipantRequest) {
        matchParticipant.createMatchParticipant(matchParticipantRequest);
        return getSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }
}