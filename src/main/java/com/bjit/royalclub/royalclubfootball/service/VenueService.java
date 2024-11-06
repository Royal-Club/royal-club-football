package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface VenueService {

    VenueResponse getById(Long venueId);

    @Transactional
    void registerVenue(VenueRegistrationRequest registrationRequest);

    List<VenueResponse> getAllVenues();

    void updateStatus(Long venueId, boolean isActive);

    VenueResponse update(Long venueId, VenueRegistrationRequest venueRequest);
}
