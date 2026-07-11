package com.bjit.royalclub.royalclubfootball.controller.auction;

import com.bjit.royalclub.royalclubfootball.model.auction.AuctionSettingsRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionSettingsResponse;
import com.bjit.royalclub.royalclubfootball.service.auction.AuctionSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("tournaments/{tournamentId}/auction/settings")
@RequiredArgsConstructor
public class AuctionSettingsController {

    private final AuctionSettingsService auctionSettingsService;

    @GetMapping
    public ResponseEntity<AuctionSettingsResponse> getSettings(@PathVariable Long tournamentId) {
        AuctionSettingsResponse settings = auctionSettingsService.getSettings(tournamentId);
        if (settings == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(settings);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSettingsResponse> createSettings(
            @PathVariable Long tournamentId,
            @Valid @RequestBody AuctionSettingsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(auctionSettingsService.createSettings(tournamentId, request));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionSettingsResponse> updateSettings(
            @PathVariable Long tournamentId,
            @Valid @RequestBody AuctionSettingsRequest request) {
        return ResponseEntity.ok(auctionSettingsService.updateSettings(tournamentId, request));
    }
}
