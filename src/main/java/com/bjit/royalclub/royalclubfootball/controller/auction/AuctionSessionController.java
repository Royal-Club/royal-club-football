package com.bjit.royalclub.royalclubfootball.controller.auction;

import com.bjit.royalclub.royalclubfootball.model.auction.*;
import com.bjit.royalclub.royalclubfootball.service.auction.AuctionSessionService;
import com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tournaments/{tournamentId}/auction")
@RequiredArgsConstructor
public class AuctionSessionController {

    private final AuctionSessionService sessionService;

    // === Session Control (Admin) ===

    @GetMapping("/session")
    public ResponseEntity<AuctionSessionResponse> getSession(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.getSession(tournamentId));
    }

    @PostMapping("/session/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> startAuction(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.startAuction(tournamentId));
    }

    @PostMapping("/session/pause")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> pauseAuction(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.pauseAuction(tournamentId));
    }

    @PostMapping("/session/resume")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> resumeAuction(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.resumeAuction(tournamentId));
    }

    @PostMapping("/session/end")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> endAuction(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.endAuction(tournamentId));
    }

    @PostMapping("/session/next-player")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> nextPlayer(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.nextPlayer(tournamentId));
    }

    @PostMapping("/session/next-player/random")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> nextPlayerRandom(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.nextPlayerRandom(tournamentId));
    }

    @PostMapping("/session/skip-player")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> skipPlayer(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.skipPlayer(tournamentId));
    }

    @PostMapping("/session/mark-sold")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> markSold(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.markSold(tournamentId));
    }

    @PostMapping("/session/mark-unsold")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> markUnsold(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.markUnsold(tournamentId));
    }

    @PostMapping("/session/undo-last-sale")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> undoLastSale(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.undoLastSale(tournamentId));
    }

    @PostMapping("/session/restart-bidding")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> restartBidding(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.restartBidding(tournamentId));
    }

    @PostMapping("/session/start-unsold-round")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSessionResponse> startUnsoldRound(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.startUnsoldRound(tournamentId));
    }

    // === Bidding ===

    @PostMapping("/bids")
    @PreAuthorize("hasAnyRole('TEAM_OWNER', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<BidResponse> placeBid(
            @PathVariable Long tournamentId,
            @Valid @RequestBody BidRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(sessionService.placeBid(tournamentId, request, userId));
    }

    @GetMapping("/bids")
    public ResponseEntity<List<BidResponse>> getAllBids(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.getAllBids(tournamentId));
    }

    @GetMapping("/players/{auctionPlayerId}/bids")
    public ResponseEntity<List<BidResponse>> getBidsForPlayer(
            @PathVariable Long tournamentId,
            @PathVariable Long auctionPlayerId) {
        return ResponseEntity.ok(sessionService.getBidsForPlayer(tournamentId, auctionPlayerId));
    }

    // === Dashboard ===

    @GetMapping("/dashboard")
    public ResponseEntity<AuctionDashboardResponse> getDashboard(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.getDashboard(tournamentId));
    }

    // === Results ===

    @GetMapping("/results")
    public ResponseEntity<AuctionResultResponse> getResults(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(sessionService.getResults(tournamentId));
    }

    private Long getCurrentUserId() {
        return SecurityUtil.getLoggedInUserId();
    }
}
