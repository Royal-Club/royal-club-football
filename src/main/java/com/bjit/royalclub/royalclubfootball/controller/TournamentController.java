package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.model.TournamentRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentUpdateRequest;
import com.bjit.royalclub.royalclubfootball.service.TeamManagementService;
import com.bjit.royalclub.royalclubfootball.service.TournamentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.STATUS_UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TeamManagementService teamManagementService;

    @PostMapping
    public ResponseEntity<Object> saveTournament(@Valid @RequestBody TournamentRequest tournamentRequest) {
        TournamentResponse tournamentResponse = tournamentService.saveTournament(tournamentRequest);
        return buildSuccessResponse(HttpStatus.OK, CREATE_OK, tournamentResponse);
    }

    @GetMapping
    public ResponseEntity<Object> getAllTournament() {
        List<TournamentResponse> tournamentResponses = tournamentService.getAllTournament();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, tournamentResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getTournamentById(@PathVariable Long id) {
        TournamentResponse tournamentResponse = tournamentService.getTournamentById(id);
        return buildSuccessResponse(HttpStatus.FOUND, CREATE_OK, tournamentResponse);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Object> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        tournamentService.updateTournamentStatus(id, active);
        return buildSuccessResponse(HttpStatus.OK, STATUS_UPDATE_OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePlayer(@PathVariable Long id, @Valid @RequestBody
    TournamentUpdateRequest tournamentUpdateRequest) {
        TournamentResponse tournamentResponse = tournamentService.updateTournament(id, tournamentUpdateRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, tournamentResponse);
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getTournamentsSummery(@RequestParam(required = false) Long tournamentId) {
        List<TournamentResponse> tournamentResponses = teamManagementService.getTournamentsSummery(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, tournamentResponses);
    }

    @GetMapping("/next-upcoming")
    public ResponseEntity<Object> getTournaments() {
        List<TournamentResponse> tournamentResponses = teamManagementService.getTournament();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, tournamentResponses);
    }

    @GetMapping("/next")
    public ResponseEntity<Object> getNextTournament() {
        Tournament tournament = tournamentService.getNextUpcomingTournament();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, tournament);
    }

}
