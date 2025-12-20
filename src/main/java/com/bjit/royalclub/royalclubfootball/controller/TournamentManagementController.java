package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRemoveRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamResponse;
import com.bjit.royalclub.royalclubfootball.service.TeamManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class TournamentManagementController {
    private final TeamManagementService teamManagementService;

    @PostMapping
    public ResponseEntity<Object> createOrUpdateTeam(@Validated @RequestBody TeamRequest teamRequest) {
        TeamResponse teamResponse = teamManagementService.createOrUpdateTeam(teamRequest);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, teamResponse);
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Object> deleteTeam(@PathVariable Long teamId) {
        teamManagementService.deleteTeam(teamId);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }

    @PostMapping("/players")
    public ResponseEntity<Object> saveOrUpdateTeamPlayer(@Valid @RequestBody TeamPlayerRequest teamPlayerRequest) {
        TeamPlayerResponse teamPlayerResponse = teamManagementService.saveOrUpdateTeamPlayer(teamPlayerRequest);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, teamPlayerResponse);
    }

    @PutMapping("/players")
    public ResponseEntity<Object> updateTeamPlayerDetails(@Valid @RequestBody TeamPlayerRequest teamPlayerRequest) {
        TeamPlayerResponse teamPlayerResponse = teamManagementService.updateTeamPlayerDetails(teamPlayerRequest);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, teamPlayerResponse);
    }

    @DeleteMapping("/players")
    public ResponseEntity<Object> removePlayerFromTeam(@Valid @RequestBody TeamPlayerRemoveRequest playerRemoveRequest) {
        teamManagementService.removePlayerFromTeam(playerRemoveRequest);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }

}
