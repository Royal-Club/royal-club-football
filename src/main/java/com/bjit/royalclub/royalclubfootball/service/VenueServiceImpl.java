package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;
import com.bjit.royalclub.royalclubfootball.repository.VenueRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    @Override
    public Venue getVenueById(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found for id: " + venueId));
    }

    @Override
    public void registerVenue(VenueRegistrationRequest registrationRequest) {
        /*Name Should be Unique. Need to check first then Save*/
        Venue venue = Venue.builder()
                .name(registrationRequest.getName().trim())
                .address(registrationRequest.getAddress())
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();
        venueRepository.save(venue);
    }

    @Override
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream().map(venue ->
                VenueResponse.builder()
                        .id(venue.getId())
                        .address(venue.getAddress())
                        .name(venue.getName())
                        .build()
        ).toList();
    }

}
