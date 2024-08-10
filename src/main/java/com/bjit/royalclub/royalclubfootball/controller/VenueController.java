package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;
import com.bjit.royalclub.royalclubfootball.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/venues")
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAllVenues() {
        List<VenueResponse> venueResponses = venueService.getAllVenues();
        return ResponseEntity.ok(venueResponses);
    }

    @PostMapping
    public ResponseEntity<Void> registerVenue(@Valid VenueRegistrationRequest venueRegistrationRequest) {
        venueService.registerVenue(venueRegistrationRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
