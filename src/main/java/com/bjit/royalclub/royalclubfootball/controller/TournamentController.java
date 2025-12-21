package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.PaginatedTournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentListResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentUpdateRequest;
import com.bjit.royalclub.royalclubfootball.service.TeamManagementService;
import com.bjit.royalclub.royalclubfootball.service.TournamentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Object> saveTournament(@Valid @RequestBody TournamentRequest tournamentRequest) {
        TournamentResponse tournamentResponse = tournamentService.saveTournament(tournamentRequest);
        return buildSuccessResponse(HttpStatus.OK, CREATE_OK, tournamentResponse);
    }

    @GetMapping
    public ResponseEntity<Object> getAllTournament(@RequestParam(defaultValue = "0") int offSet,
                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                   @RequestParam(defaultValue = "id") String sortedBy,
                                                   @RequestParam(defaultValue = "DESC") String sortDirection,
                                                   @RequestParam(required = false) String searchColumn,
                                                   @RequestParam(required = false) String searchValue) {
        PaginatedTournamentResponse tournamentResponses
                = tournamentService.getAllTournament(offSet, pageSize, sortedBy, sortDirection, searchColumn, searchValue);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, tournamentResponses);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Object> getTournamentById(@PathVariable Long id) {
        TournamentResponse tournamentResponse = tournamentService.getTournamentById(id);
        return buildSuccessResponse(HttpStatus.FOUND, CREATE_OK, tournamentResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Object> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        tournamentService.updateTournamentStatus(id, active);
        return buildSuccessResponse(HttpStatus.OK, STATUS_UPDATE_OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}/conclude")
    public ResponseEntity<Object> concludeTournament(@PathVariable Long id) {
        tournamentService.concludeTournament(id);
        return buildSuccessResponse(HttpStatus.OK, "Tournament concluded successfully");
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTournament(@PathVariable Long id, @Valid @RequestBody
    TournamentUpdateRequest tournamentUpdateRequest) {
        TournamentResponse tournamentResponse = tournamentService.updateTournament(id, tournamentUpdateRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, tournamentResponse);
    }

    @GetMapping("/details")
    public ResponseEntity<Object> getTournamentsSummery(@RequestParam Long tournamentId) {
        List<TournamentResponse> tournamentResponses = teamManagementService.getTournamentsSummery(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, tournamentResponses);
    }

    @GetMapping("/most-recent")
    public ResponseEntity<Object> getMostRecentTournament() {
        TournamentResponse response = tournamentService.getMostRecentTournament();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, response);
    }

    /**
     * Get list of tournaments (id, name, and tournamentDate) ordered by tournament date descending
     *
     * @param year Optional year filter in format "YYYY" (e.g., "2025" returns tournaments from year 2025)
     * @return List of tournaments with id, name, and tournamentDate
     */
    @GetMapping("/list")
    public ResponseEntity<Object> getTournamentList(@RequestParam(required = false) String year) {
        List<TournamentListResponse> tournaments = tournamentService.getTournamentList(year);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, tournaments);
    }

    /**
     * Get list of unique years where tournaments exist
     * Returns years in format "YYYY" (e.g., "2025", "2024")
     * Ordered by year descending (newest first)
     *
     * @return List of year strings
     */
    @GetMapping("/sessions")
    public ResponseEntity<Object> getTournamentSessions() {
        List<String> sessions = tournamentService.getTournamentSessions();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, sessions);
    }
}
