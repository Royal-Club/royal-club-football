package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;
import com.bjit.royalclub.royalclubfootball.service.VenueService;
import com.bjit.royalclub.royalclubfootball.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/venues")
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<Object> getAllVenues() {
        List<VenueResponse> venueResponses = venueService.getAllVenues();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, venueResponses);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Object> registerVenue(@Valid VenueRegistrationRequest venueRegistrationRequest) {
        venueService.registerVenue(venueRegistrationRequest);
        return ResponseBuilder.buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateVenue(@PathVariable Long id,
                                              @Valid @RequestBody VenueRegistrationRequest venueRegistrationRequest) {
        VenueResponse updatedVenueResponse = venueService.update(id, venueRegistrationRequest);
        return ResponseBuilder.buildSuccessResponse(HttpStatus.OK, UPDATE_OK, updatedVenueResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Object> updateVenueStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        venueService.updateStatus(id, isActive);
        return ResponseBuilder.buildSuccessResponse(HttpStatus.OK, STATUS_UPDATE_OK);
    }
}
