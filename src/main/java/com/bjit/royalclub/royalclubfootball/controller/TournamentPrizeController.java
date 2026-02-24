package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.enums.PrizeType;
import com.bjit.royalclub.royalclubfootball.model.TournamentPrizeRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentPrizeResponse;
import com.bjit.royalclub.royalclubfootball.service.TournamentPrizeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/tournaments/{tournamentId}/prizes")
public class TournamentPrizeController {

    private final TournamentPrizeService prizeService;

    /**
     * Create a new prize for a tournament (Admin only)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'COORDINATOR')")
    @PostMapping
    public ResponseEntity<Object> createPrize(
            @PathVariable Long tournamentId,
            @Valid @RequestBody TournamentPrizeRequest request) {
        request.setTournamentId(tournamentId);
        TournamentPrizeResponse response = prizeService.createPrize(request);
        return buildSuccessResponse(HttpStatus.CREATED, "Prize created successfully", response);
    }

    /**
     * Update an existing prize (Admin only)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'COORDINATOR')")
    @PutMapping("/{prizeId}")
    public ResponseEntity<Object> updatePrize(
            @PathVariable Long tournamentId,
            @PathVariable Long prizeId,
            @Valid @RequestBody TournamentPrizeRequest request) {
        request.setTournamentId(tournamentId);
        TournamentPrizeResponse response = prizeService.updatePrize(prizeId, request);
        return buildSuccessResponse(HttpStatus.OK, "Prize updated successfully", response);
    }

    /**
     * Delete a prize (Admin only)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN', 'COORDINATOR')")
    @DeleteMapping("/{prizeId}")
    public ResponseEntity<Object> deletePrize(
            @PathVariable Long tournamentId,
            @PathVariable Long prizeId) {
        prizeService.deletePrize(prizeId);
        return buildSuccessResponse(HttpStatus.OK, "Prize deleted successfully");
    }

    /**
     * Get a single prize by ID
     */
    @GetMapping("/{prizeId}")
    public ResponseEntity<Object> getPrizeById(
            @PathVariable Long tournamentId,
            @PathVariable Long prizeId) {
        TournamentPrizeResponse response = prizeService.getPrizeById(prizeId);
        return buildSuccessResponse(HttpStatus.OK, "Prize fetched successfully", response);
    }

    /**
     * Get all prizes for a tournament
     */
    @GetMapping
    public ResponseEntity<Object> getAllPrizes(@PathVariable Long tournamentId) {
        List<TournamentPrizeResponse> prizes = prizeService.getAllPrizesByTournament(tournamentId);
        return buildSuccessResponse(HttpStatus.OK, "Prizes fetched successfully", prizes);
    }

    /**
     * Get prizes by type (TEAM or PLAYER)
     */
    @GetMapping("/by-type")
    public ResponseEntity<Object> getPrizesByType(
            @PathVariable Long tournamentId,
            @RequestParam PrizeType prizeType) {
        List<TournamentPrizeResponse> prizes = prizeService.getPrizesByTournamentAndType(tournamentId, prizeType);
        return buildSuccessResponse(HttpStatus.OK, "Prizes fetched successfully", prizes);
    }

    /**
     * Get prizes for a specific team
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<Object> getPrizesByTeam(
            @PathVariable Long tournamentId,
            @PathVariable Long teamId) {
        List<TournamentPrizeResponse> prizes = prizeService.getPrizesByTeam(tournamentId, teamId);
        return buildSuccessResponse(HttpStatus.OK, "Team prizes fetched successfully", prizes);
    }

    /**
     * Get prizes for a specific player
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<Object> getPrizesByPlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long playerId) {
        List<TournamentPrizeResponse> prizes = prizeService.getPrizesByPlayer(tournamentId, playerId);
        return buildSuccessResponse(HttpStatus.OK, "Player prizes fetched successfully", prizes);
    }
}
