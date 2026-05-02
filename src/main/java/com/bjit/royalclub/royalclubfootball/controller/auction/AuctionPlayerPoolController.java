package com.bjit.royalclub.royalclubfootball.controller.auction;

import com.bjit.royalclub.royalclubfootball.model.auction.AuctionPlayerRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionPlayerResponse;
import com.bjit.royalclub.royalclubfootball.service.auction.AuctionPlayerPoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tournaments/{tournamentId}/auction/players")
@RequiredArgsConstructor
public class AuctionPlayerPoolController {

    private final AuctionPlayerPoolService playerPoolService;

    @GetMapping
    public ResponseEntity<List<AuctionPlayerResponse>> getPlayerPool(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(playerPoolService.getPlayerPool(tournamentId));
    }

    @PostMapping("/add-existing")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionPlayerResponse> addExistingPlayer(
            @PathVariable Long tournamentId,
            @Valid @RequestBody AuctionPlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerPoolService.addExistingPlayer(tournamentId, request));
    }

    @PostMapping("/add-from-registration/{registrationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionPlayerResponse> addFromRegistration(
            @PathVariable Long tournamentId,
            @PathVariable Long registrationId,
            @RequestBody AuctionPlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerPoolService.addFromRegistration(tournamentId, registrationId, request));
    }

    @PutMapping("/{auctionPlayerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionPlayerResponse> updatePlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long auctionPlayerId,
            @Valid @RequestBody AuctionPlayerRequest request) {
        return ResponseEntity.ok(playerPoolService.updatePlayer(tournamentId, auctionPlayerId, request));
    }

    @DeleteMapping("/{auctionPlayerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Void> removePlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long auctionPlayerId) {
        playerPoolService.removePlayer(tournamentId, auctionPlayerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{auctionPlayerId}/restore")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionPlayerResponse> restorePlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long auctionPlayerId) {
        return ResponseEntity.ok(playerPoolService.restorePlayer(tournamentId, auctionPlayerId));
    }
}
