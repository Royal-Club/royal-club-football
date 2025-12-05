package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.MatchEventRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchEventResponse;
import com.bjit.royalclub.royalclubfootball.model.MatchResponse;
import com.bjit.royalclub.royalclubfootball.model.MatchUpdateRequest;
import com.bjit.royalclub.royalclubfootball.model.Response;
import com.bjit.royalclub.royalclubfootball.service.MatchManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
@Validated
public class MatchController {

    private final MatchManagementService matchManagementService;

    @GetMapping("/{matchId}")
    public ResponseEntity<Response> getMatchById(@PathVariable Long matchId) {
        MatchResponse match = matchManagementService.getMatchById(matchId);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match retrieved successfully")
                .content(match)
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @PostMapping("/{matchId}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> startMatch(@PathVariable Long matchId) {
        MatchResponse match = matchManagementService.startMatch(matchId);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match started successfully")
                .content(match)
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @PostMapping("/{matchId}/pause")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> pauseMatch(@PathVariable Long matchId) {
        MatchResponse match = matchManagementService.pauseMatch(matchId);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match paused successfully")
                .content(match)
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @PostMapping("/{matchId}/resume")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> resumeMatch(@PathVariable Long matchId) {
        MatchResponse match = matchManagementService.resumeMatch(matchId);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match resumed successfully")
                .content(match)
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @PostMapping("/{matchId}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> completeMatch(@PathVariable Long matchId) {
        MatchResponse match = matchManagementService.completeMatch(matchId);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match completed successfully")
                .content(match)
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @PutMapping("/{matchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateMatch(@PathVariable Long matchId,
                                                @Valid @RequestBody MatchUpdateRequest updateRequest) {
        MatchResponse match = matchManagementService.updateMatch(matchId, updateRequest);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match updated successfully")
                .content(match)
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @GetMapping("/{matchId}/events")
    public ResponseEntity<Response> getMatchEvents(@PathVariable Long matchId) {
        var events = matchManagementService.getMatchEvents(matchId);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match events retrieved successfully")
                .content(events)
                .numberOfElement(events.size())
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @PostMapping("/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> recordMatchEvent(@Valid @RequestBody MatchEventRequest eventRequest) {
        MatchEventResponse event = matchManagementService.recordMatchEvent(eventRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.builder()
                        .statusCode(HttpStatus.CREATED.value())
                        .status("CREATED")
                        .message("Match event recorded successfully")
                        .content(event)
                        .timeStamp(System.currentTimeMillis())
                        .build());
    }

    @PutMapping("/{matchId}/elapsed-time")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateElapsedTime(@PathVariable Long matchId,
                                                      @RequestParam Integer elapsedSeconds) {
        matchManagementService.updateElapsedTime(matchId, elapsedSeconds);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Elapsed time updated successfully")
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @PutMapping("/{matchId}/score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> updateTeamScore(@PathVariable Long matchId,
                                                    @RequestParam Long teamId,
                                                    @RequestParam Integer newScore) {
        matchManagementService.updateTeamScore(matchId, teamId, newScore);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Team score updated successfully")
                .timeStamp(System.currentTimeMillis())
                .build());
    }

    @DeleteMapping("/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteMatchEvent(@PathVariable Long eventId) {
        matchManagementService.deleteMatchEvent(eventId);
        return ResponseEntity.ok(Response.builder()
                .statusCode(HttpStatus.OK.value())
                .status("OK")
                .message("Match event deleted successfully")
                .timeStamp(System.currentTimeMillis())
                .build());
    }

}
