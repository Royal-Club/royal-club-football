package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.exception.VenueServiceException;
import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;
import com.bjit.royalclub.royalclubfootball.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.VENUE_IS_ALREADY_EXISTS;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.VENUE_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    @Override
    public VenueResponse getById(Long venueId) {
        Venue venue = getVenueById(venueId);
        return convertToDto(venue);
    }

    @Override
    public void registerVenue(VenueRegistrationRequest venueRegistrationRequest) {
        String venueName = venueRegistrationRequest.getName().trim();

        venueRepository.findByName(venueName).ifPresent(venue -> {
            throw new VenueServiceException(VENUE_IS_ALREADY_EXISTS, HttpStatus.CONFLICT);
        });

        Venue venue = Venue.builder()
                .name(venueName)
                .address(venueRegistrationRequest.getAddress())
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();
        venueRepository.save(venue);
    }

    @Override
    public List<VenueResponse> getVenues() {
        return venueRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public void updateStatus(Long venueId, boolean isActive) {
        Venue venue = getVenueById(venueId);
        venue.setActive(isActive);
        venue.setUpdatedDate(LocalDateTime.now());
        venueRepository.save(venue);
    }

    @Override
    public VenueResponse update(Long venueId, VenueRegistrationRequest venueRequest) {
        Venue venue = getVenueById(venueId);

        String trimmedName = venueRequest.getName().trim();
        venue.setName(trimmedName);
        venue.setAddress(venueRequest.getAddress());
        venue.setUpdatedDate(LocalDateTime.now());

        Venue updatedVenue = venueRepository.save(venue);
        return convertToDto(updatedVenue);
    }

    private Venue getVenueById(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueServiceException(VENUE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private VenueResponse convertToDto(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .active(venue.isActive())
                .build();
    }
}
