package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import com.bjit.royalclub.royalclubfootball.service.TeamManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TournamentManagementController {
    private final TeamManagementService teamManagementService;

    @PostMapping
    public ResponseEntity<Object> createOrUpdateTeam(@Validated @RequestBody TeamRequest teamRequest) {
        TeamResponse teamResponse = teamManagementService.createOrUpdateTeam(teamRequest);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, teamResponse);
    }
}
