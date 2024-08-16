package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.MatchScheduleRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchScheduleResponse;
import com.bjit.royalclub.royalclubfootball.service.MatchScheduleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/matches-schedule")
public class MatchScheduleController {

    private final MatchScheduleService matchScheduleService;

    @GetMapping("/upcoming")
    public ResponseEntity<Object> getUpcomingMatches() {
        List<MatchScheduleResponse> matches = matchScheduleService.getUpcomingMatches();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, matches);
    }

    @PostMapping
    public ResponseEntity<Object> saveMatch(@Valid @RequestBody MatchScheduleRequest matchScheduleRequest) {
        MatchScheduleResponse matchScheduleResponse = matchScheduleService.saveMatch(matchScheduleRequest);
        return buildSuccessResponse(HttpStatus.OK, CREATE_OK, matchScheduleResponse);
    }
}
