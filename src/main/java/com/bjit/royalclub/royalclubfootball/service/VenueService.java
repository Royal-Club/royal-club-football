package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;

import java.util.List;

public interface VenueService {

    Venue getVenueById(Long venueId);

    void registerVenue(VenueRegistrationRequest registrationRequest);

    List<VenueResponse> getAllVenues();
}
