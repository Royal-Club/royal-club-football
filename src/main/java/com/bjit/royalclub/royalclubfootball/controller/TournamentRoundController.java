package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.*;
import com.bjit.royalclub.royalclubfootball.service.TournamentRoundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.*;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("rounds")
public class TournamentRoundController {

    private final TournamentRoundService tournamentRoundService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Object> createRound(@Valid @RequestBody TournamentRoundRequest request) {
        TournamentRoundResponse response = tournamentRoundService.createRound(request);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{roundId}")
    public ResponseEntity<Object> updateRound(
            @PathVariable Long roundId,
            @Valid @RequestBody TournamentRoundRequest request) {
        TournamentRoundResponse response = tournamentRoundService.updateRound(roundId, request);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{roundId}")
    public ResponseEntity<Object> deleteRound(@PathVariable Long roundId) {
        tournamentRoundService.deleteRound(roundId);
        return buildSuccessResponse(HttpStatus.OK, "Round deleted successfully");
    }

    @GetMapping("/{roundId}")
    public ResponseEntity<Object> getRoundById(@PathVariable Long roundId) {
        TournamentRoundResponse response = tournamentRoundService.getRoundById(roundId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<Object> getRoundsByTournamentId(@PathVariable Long tournamentId) {
        List<TournamentRoundResponse> responses = tournamentRoundService.getRoundsByTournamentId(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, responses);
    }

    @GetMapping("/tournament/{tournamentId}/structure")
    public ResponseEntity<Object> getTournamentStructure(@PathVariable Long tournamentId) {
        TournamentStructureResponse response = tournamentRoundService.getTournamentStructure(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{roundId}/start")
    public ResponseEntity<Object> startRound(@PathVariable Long roundId) {
        TournamentRoundResponse response = tournamentRoundService.startRound(roundId);
        return buildSuccessResponse(HttpStatus.OK, "Round started successfully", response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/complete")
    public ResponseEntity<Object> completeRound(@Valid @RequestBody RoundCompletionRequest request) {
        AdvancedTeamsResponse response = tournamentRoundService.completeRound(request);
        return buildSuccessResponse(HttpStatus.OK, "Round completed successfully", response);
    }

    @GetMapping("/tournament/{tournamentId}/next")
    public ResponseEntity<Object> getNextRound(
            @PathVariable Long tournamentId,
            @RequestParam Integer currentSequenceOrder) {
        TournamentRoundResponse response = tournamentRoundService.getNextRound(tournamentId, currentSequenceOrder);
        if (response == null) {
            return buildSuccessResponse(HttpStatus.NOT_FOUND, "No next round found", null);
        }
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

    @GetMapping("/tournament/{tournamentId}/previous")
    public ResponseEntity<Object> getPreviousRound(
            @PathVariable Long tournamentId,
            @RequestParam Integer currentSequenceOrder) {
        TournamentRoundResponse response = tournamentRoundService.getPreviousRound(tournamentId, currentSequenceOrder);
        if (response == null) {
            return buildSuccessResponse(HttpStatus.NOT_FOUND, "No previous round found", null);
        }
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{roundId}/teams")
    public ResponseEntity<Object> assignTeamsToRound(
            @PathVariable Long roundId,
            @Valid @RequestBody TeamAssignmentRequest request) {
        tournamentRoundService.assignTeamsToRound(roundId, request);
        return buildSuccessResponse(HttpStatus.OK, "Teams assigned to round successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{roundId}/teams/{teamId}")
    public ResponseEntity<Object> removeTeamFromRound(
            @PathVariable Long roundId,
            @PathVariable Long teamId) {
        tournamentRoundService.removeTeamFromRound(roundId, teamId);
        return buildSuccessResponse(HttpStatus.OK, "Team removed from round successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/{roundId}/matches/generate")
    public ResponseEntity<Object> generateRoundMatches(
            @PathVariable Long roundId,
            @Valid @RequestBody RoundMatchGenerationRequest request) {
        List<MatchResponse> matches = tournamentRoundService.generateRoundMatches(roundId, request);
        return buildSuccessResponse(HttpStatus.OK, "Matches generated successfully", matches);
    }
}
