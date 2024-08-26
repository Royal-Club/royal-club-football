package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.GoalkeeperStatsResponse;
import com.bjit.royalclub.royalclubfootball.model.PlayerParticipationResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentParticipantRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentWithPlayersResponse;
import com.bjit.royalclub.royalclubfootball.service.TournamentParticipantPlayerService;
import com.bjit.royalclub.royalclubfootball.service.TournamentParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("tournament-participants")
public class TournamentParticipantController {
    private final TournamentParticipantService tournamentParticipantService;
    private final TournamentParticipantPlayerService tournamentParticipantPlayerService;

    @PostMapping
    public ResponseEntity<Object> saveTournamentParticipant(@Valid @RequestBody TournamentParticipantRequest tournamentParticipantRequest) {
        tournamentParticipantService.updateTournamentParticipant(tournamentParticipantRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }

    @GetMapping
    public ResponseEntity<Object> getAllTournamentsWithPlayers() {
        List<TournamentWithPlayersResponse> withPlayersResponse =
                tournamentParticipantPlayerService.getAllTournamentsWithPlayers();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, withPlayersResponse);
    }

    @GetMapping("/next-upcoming")
    public ResponseEntity<Object> getNextSingleTournamentWithPlayers() {
        TournamentWithPlayersResponse tournamentWithPlayersResponse =
                tournamentParticipantPlayerService.findNextSingleTournamentWithPlayers();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, tournamentWithPlayersResponse);
    }

    @GetMapping("{tournamentId}/to-be-selected")
    public ResponseEntity<Object> playersToBeSelectedForTeam(@PathVariable Long tournamentId) {
        List<PlayerParticipationResponse> participationResponses =
                tournamentParticipantService.playersToBeSelectedForTeam(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, participationResponses);
    }

    @GetMapping("/{tournamentId}/goal-keepers")
    public ResponseEntity<Object> test(@PathVariable Long tournamentId) {
        List<GoalkeeperStatsResponse> goalkeeperStatsResponses =
                tournamentParticipantService.goalkeeperStatsResponse(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, goalkeeperStatsResponses);
    }
}
