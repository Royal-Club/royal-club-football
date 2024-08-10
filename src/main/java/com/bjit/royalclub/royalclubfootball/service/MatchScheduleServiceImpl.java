package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.MatchSchedule;
import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.model.MatchScheduleRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchScheduleResponse;
import com.bjit.royalclub.royalclubfootball.repository.MatchScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchScheduleServiceImpl implements MatchScheduleService {

    private final MatchScheduleRepository matchScheduleRepository;
    private final VenueService venueService;

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
        Venue venue = venueService.getVenueById(matchScheduleRequest.getVenueId());

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
