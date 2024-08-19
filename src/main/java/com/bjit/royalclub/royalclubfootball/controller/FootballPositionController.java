package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.FootballPositionResponse;
import com.bjit.royalclub.royalclubfootball.service.FootballPositionService;
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
@RequestMapping("/football-positions")
public class FootballPositionController {

    private final FootballPositionService footballPositionService;

    @GetMapping
    public ResponseEntity<Object> getAllFootballPositions() {
        List<FootballPositionResponse> positions = footballPositionService.getAllPositions();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, positions);
    }
}
