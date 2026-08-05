package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.TeamFormationRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamFormationResponse;
import com.bjit.royalclub.royalclubfootball.service.TeamFormationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

/**
 * Team line-ups. Every signed-in member can look at any formation — seeing who
 * is playing where is the point of the feature. Saving is restricted to the
 * team's captain and tournament admins, which the service enforces per team
 * rather than by role alone.
 */
@RestController
@RequestMapping("formations")
@RequiredArgsConstructor
public class TeamFormationController {

    private final TeamFormationService teamFormationService;

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<Object> defaultFormation(@PathVariable Long teamId) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, teamFormationService.getDefaultFormation(teamId));
    }

    @PutMapping("/teams/{teamId}")
    public ResponseEntity<Object> saveDefaultFormation(@PathVariable Long teamId,
                                                       @Valid @RequestBody TeamFormationRequest request) {
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK,
                teamFormationService.saveDefaultFormation(teamId, request));
    }

    @GetMapping("/matches/{matchId}/teams/{teamId}")
    public ResponseEntity<Object> matchFormation(@PathVariable Long matchId, @PathVariable Long teamId) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK,
                teamFormationService.getMatchFormation(teamId, matchId));
    }

    @PutMapping("/matches/{matchId}/teams/{teamId}")
    public ResponseEntity<Object> saveMatchFormation(@PathVariable Long matchId,
                                                     @PathVariable Long teamId,
                                                     @Valid @RequestBody TeamFormationRequest request) {
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK,
                teamFormationService.saveMatchFormation(teamId, matchId, request));
    }

    /** Drops the match line-up so the team falls back to its default. */
    @DeleteMapping("/matches/{matchId}/teams/{teamId}")
    public ResponseEntity<Object> resetMatchFormation(@PathVariable Long matchId, @PathVariable Long teamId) {
        teamFormationService.resetMatchFormation(teamId, matchId);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }

    /** Both sides of a fixture, for the match page. */
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<Object> matchFormations(@PathVariable Long matchId) {
        List<TeamFormationResponse> responses = teamFormationService.getMatchFormations(matchId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, responses, responses.size());
    }
}
