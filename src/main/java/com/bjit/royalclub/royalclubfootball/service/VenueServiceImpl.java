package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.exception.VenueServiceException;
import com.bjit.royalclub.royalclubfootball.model.VenueRegistrationRequest;
import com.bjit.royalclub.royalclubfootball.model.VenueResponse;
import com.bjit.royalclub.royalclubfootball.repository.VenueRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.VENUE_IS_ALREADY_EXISTS;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.VENUE_IS_NOT_FOUND;

@Service
@AllArgsConstructor
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    @Override
    public VenueResponse getById(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueServiceException(VENUE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        return convertToDto(venue);
    }

    @Override
    public void registerVenue(VenueRegistrationRequest venueRegistrationRequest) {

        venueRepository.findByName(venueRegistrationRequest.getName()).ifPresent(venue -> {
            throw new VenueServiceException(VENUE_IS_ALREADY_EXISTS, HttpStatus.CONFLICT);
        });

        Venue venue = Venue.builder()
                .name(venueRegistrationRequest.getName().trim())
                .address(venueRegistrationRequest.getAddress())
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();
        venueRepository.save(venue);
    }

    @Override
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public void updateStatus(Long venueId, boolean isActive) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueServiceException(VENUE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        venue.setActive(isActive);
        venue.setUpdatedDate(LocalDateTime.now());
        venueRepository.save(venue);
    }

    @Override
    public VenueResponse update(Long venueId, VenueRegistrationRequest venueRequest) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueServiceException(VENUE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        venue.setName(venueRequest.getName().trim());
        venue.setAddress(venueRequest.getAddress());
        venue.setUpdatedDate(LocalDateTime.now());

        Venue updatedVenue = venueRepository.save(venue);

        return convertToDto(updatedVenue);
    }

    private VenueResponse convertToDto(Venue venue) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .build();
    }

}
