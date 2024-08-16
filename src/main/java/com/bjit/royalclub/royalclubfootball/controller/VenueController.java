package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;
import com.bjit.royalclub.royalclubfootball.service.VenueService;
import com.bjit.royalclub.royalclubfootball.util.ResponseBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
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

    @PostMapping
    public ResponseEntity<Object> registerVenue(@Valid VenueRegistrationRequest venueRegistrationRequest) {
        venueService.registerVenue(venueRegistrationRequest);
        return ResponseBuilder.buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }
}
