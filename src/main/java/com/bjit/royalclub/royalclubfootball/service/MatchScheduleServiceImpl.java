package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.MatchSchedule;
import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.exception.VenueServiceException;
import com.bjit.royalclub.royalclubfootball.model.MatchScheduleRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchScheduleResponse;
import com.bjit.royalclub.royalclubfootball.repository.MatchScheduleRepository;
import com.bjit.royalclub.royalclubfootball.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.VENUE_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MatchScheduleServiceImpl implements MatchScheduleService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final VenueRepository venueRepository;

    @Override
    public List<MatchScheduleResponse> getUpcomingMatches() {
        LocalDateTime now = LocalDateTime.now();
        return matchScheduleRepository.findByDateTimeAfter(now).stream()
                .map(this::convertToDto)
                .toList();
    }

    private MatchScheduleResponse convertToDto(MatchSchedule matchSchedule) {
        return MatchScheduleResponse.builder()
                .id(matchSchedule.getId())
                .dateTime(matchSchedule.getDateTime().toLocalDate())
                .venueName(matchSchedule.getVenue().getName())
                .build();
    }

    @Override
    public MatchScheduleResponse saveMatch(MatchScheduleRequest matchScheduleRequest) {

        LocalDateTime dateTime = LocalDateTime.of(matchScheduleRequest.getDate(), matchScheduleRequest.getTime());

        Venue venue = venueRepository.findById(matchScheduleRequest.getVenueId())
                .orElseThrow(() -> new VenueServiceException(VENUE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        MatchSchedule matchSchedule = MatchSchedule.builder()
                .dateTime(dateTime)
                .venue(venue)
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();
        MatchSchedule savedMatchSchedule = matchScheduleRepository.save(matchSchedule);
        return convertToDto(savedMatchSchedule);
    }
}
