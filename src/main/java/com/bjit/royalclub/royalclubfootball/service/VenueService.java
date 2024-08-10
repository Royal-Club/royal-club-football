package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface VenueService {

    Venue getVenueById(Long venueId);

    @Transactional
    void registerVenue(VenueRegistrationRequest registrationRequest);

    List<VenueResponse> getAllVenues();
}
