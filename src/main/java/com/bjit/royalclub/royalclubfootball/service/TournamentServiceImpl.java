package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.exception.VenueServiceException;
import com.bjit.royalclub.royalclubfootball.model.TournamentRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentUpdateRequest;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.VENUE_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.util.StringUtils.normalizeString;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final VenueRepository venueRepository;

    @Override
    public List<TournamentResponse> getUpcomingTournament() {
        LocalDateTime now = LocalDateTime.now();
        return tournamentRepository.findByTournamentDateAfter(now).stream()
                .map(this::convertToDto)
                .toList();
    }

    private TournamentResponse convertToDto(Tournament tournament) {
        return TournamentResponse.builder()
                .id(tournament.getId())
                .tournamentName(tournament.getName())
                .tournamentDate(tournament.getTournamentDate())
                .activeStatus(tournament.isActive())
                .venueName(tournament.getVenue().getName())
                .build();
    }

    @Override
    public TournamentResponse saveTournament(TournamentRequest tournamentRequest) {

        Venue venue = venueRepository.findById(tournamentRequest.getVenueId())
                .orElseThrow(() -> new VenueServiceException(VENUE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        Tournament tournament = Tournament.builder()
                .name(normalizeString(tournamentRequest.getTournamentName()))
                .tournamentDate(tournamentRequest.getTournamentDate())
                .venue(venue)
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();
        Tournament savedTournament = tournamentRepository.save(tournament);
        return convertToDto(savedTournament);
    }

    @Override
    public TournamentResponse getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        return convertToDto(tournament);
    }

    @Override
    public void updateTournamentStatus(Long id, boolean active) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        tournament.setActive(active);
        tournament.setUpdatedDate(LocalDateTime.now());
        tournamentRepository.save(tournament);
    }

    @Override
    public List<TournamentResponse> getAllTournament() {
        return tournamentRepository.findAll().stream()
                .map(this::convertToDto).toList();
    }

    @Override
    public TournamentResponse updateTournament(Long id, TournamentUpdateRequest tournamentUpdateRequest) {
        Tournament tournament;
        tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        Venue venue = venueRepository.findById(tournamentUpdateRequest.getVenueId())
                .orElseThrow(() -> new VenueServiceException(VENUE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        tournament.setName(normalizeString(tournamentUpdateRequest.getTournamentName()));
        tournament.setTournamentDate(tournamentUpdateRequest.getTournamentDate());
        tournament.setVenue(venue);
        tournament.setUpdatedDate(LocalDateTime.now());
        tournament = tournamentRepository.save(tournament);
        return convertToDto(tournament);
    }

}