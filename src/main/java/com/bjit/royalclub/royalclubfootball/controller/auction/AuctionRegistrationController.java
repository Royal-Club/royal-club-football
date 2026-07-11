package com.bjit.royalclub.royalclubfootball.controller.auction;

import com.bjit.royalclub.royalclubfootball.model.auction.ApproveAndPoolRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.auction.AuctionRegistrationResponse;
import com.bjit.royalclub.royalclubfootball.service.auction.AuctionRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("auction")
@RequiredArgsConstructor
public class AuctionRegistrationController {

    private final AuctionRegistrationService registrationService;

    @PostMapping("/tournaments/{tournamentId}/register")
    public ResponseEntity<AuctionRegistrationResponse> register(
            @PathVariable Long tournamentId,
            @Valid @RequestBody AuctionRegistrationRequest request) {
        request.setTournamentId(tournamentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(request));
    }

    @PostMapping("/tournaments/{tournamentId}/quick-register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuctionRegistrationResponse> quickRegister(@PathVariable Long tournamentId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.quickRegisterExistingPlayer(tournamentId));
    }

    @GetMapping("/registrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<AuctionRegistrationResponse>> getRegistrations(
            @RequestParam(required = false) Long tournamentId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(registrationService.getRegistrations(tournamentId, status));
    }

    @GetMapping("/registrations/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionRegistrationResponse> getRegistration(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.getRegistration(id));
    }

    @PostMapping("/registrations/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionRegistrationResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.approve(id));
    }

    @PostMapping("/registrations/{id}/approve-and-pool")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionRegistrationResponse> approveAndAddToPool(
            @PathVariable Long id,
            @RequestBody ApproveAndPoolRequest request) {
        return ResponseEntity.ok(registrationService.approveAndAddToPool(id, request.getCategory(), request.getBasePrice()));
    }

    @PostMapping("/registrations/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionRegistrationResponse> reject(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(registrationService.reject(id, body.get("reason")));
    }

    @PostMapping("/registrations/{id}/undo-reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<AuctionRegistrationResponse> undoReject(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.undoReject(id));
    }
}
