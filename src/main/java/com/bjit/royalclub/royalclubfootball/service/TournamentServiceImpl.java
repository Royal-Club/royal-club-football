package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.Venue;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.exception.VenueServiceException;
import com.bjit.royalclub.royalclubfootball.model.PaginatedTournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentUpdateRequest;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentSpecification;
import com.bjit.royalclub.royalclubfootball.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.VENUE_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.CONCLUDED;
import static com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.ONGOING;
import static com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.UPCOMING;
import static com.bjit.royalclub.royalclubfootball.util.PaginationUtil.createPageable;
import static com.bjit.royalclub.royalclubfootball.util.StringUtils.normalizeString;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final VenueRepository venueRepository;
    private final TournamentSpecification tournamentSpecification;

    private TournamentResponse convertToDto(Tournament tournament) {
        return TournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .tournamentDate(tournament.getTournamentDate())
                .activeStatus(tournament.isActive())
                .tournamentStatus(tournament.getTournamentStatus())
                .venueName(tournament.getVenue().getName())
                .sportType(tournament.getSportType() != null ? tournament.getSportType().toString() : null)
                .tournamentType(tournament.getTournamentType() != null ? tournament.getTournamentType().toString() : null)
                .groupCount(tournament.getGroupCount())
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
                .tournamentStatus(tournamentRequest
                        .getTournamentDate().isAfter(LocalDateTime.now()) ? UPCOMING : ONGOING)
                .isActive(true)
                .sportType(tournamentRequest.getSportType() != null ?
                        com.bjit.royalclub.royalclubfootball.enums.SportType.valueOf(tournamentRequest.getSportType()) :
                        com.bjit.royalclub.royalclubfootball.enums.SportType.FOOTBALL)
                .tournamentType(tournamentRequest.getTournamentType() != null ?
                        com.bjit.royalclub.royalclubfootball.enums.TournamentType.valueOf(tournamentRequest.getTournamentType()) :
                        com.bjit.royalclub.royalclubfootball.enums.TournamentType.ROUND_ROBIN)
                .groupCount(tournamentRequest.getGroupCount())
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
        tournamentRepository.save(tournament);
    }

    @Override
    public PaginatedTournamentResponse getAllTournament(int offSet, int pageSize,
                                                        String sortedBy, String sortDirection,
                                                        String searchColumn, String searchValue) {

        Pageable pageable = createPageable(offSet, pageSize, sortedBy, sortDirection);
        Specification<Tournament> spec = null;

        if ("tournamentDate".equalsIgnoreCase(searchColumn)) {
            LocalDateTime searchDate = LocalDateTime.parse(searchValue);
            spec = tournamentSpecification.hasDate(searchColumn, searchDate);
        } else if ("name".equalsIgnoreCase(searchColumn)) {
            spec = tournamentSpecification.hasValue(searchColumn, searchValue);
        }
        Page<Tournament> tournamentPage = tournamentRepository.findAll(spec, pageable);

        List<TournamentResponse> tournamentResponses = tournamentPage.getContent().stream()
                .map(this::convertToDto)
                .toList();

        long totalCount = tournamentPage.getTotalElements();

        return new PaginatedTournamentResponse(tournamentResponses, totalCount);
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
        if (tournamentUpdateRequest.getSportType() != null) {
            tournament.setSportType(com.bjit.royalclub.royalclubfootball.enums.SportType.valueOf(tournamentUpdateRequest.getSportType()));
        }
        if (tournamentUpdateRequest.getTournamentType() != null) {
            tournament.setTournamentType(com.bjit.royalclub.royalclubfootball.enums.TournamentType.valueOf(tournamentUpdateRequest.getTournamentType()));
        }
        if (tournamentUpdateRequest.getGroupCount() != null) {
            tournament.setGroupCount(tournamentUpdateRequest.getGroupCount());
        }
        tournament = tournamentRepository.save(tournament);
        return convertToDto(tournament);
    }

    @Override
    public void updateTournamentStatuses() {
        // Process tournaments without fixtures (use old date-based logic)
        List<Tournament> tournamentsWithoutMatches = tournamentRepository.findActiveTournamentsWithoutMatches();
        for (Tournament tournament : tournamentsWithoutMatches) {
            if (tournament.getTournamentDate().isBefore(LocalDateTime.now())) {
                tournament.setTournamentStatus(CONCLUDED);
                tournament.setActive(false);
                tournamentRepository.save(tournament);
            }
        }

        // Process tournaments with fixtures (use match-based logic)
        List<Tournament> tournamentsWithMatches = tournamentRepository.findActiveTournamentsWithMatches();
        for (Tournament tournament : tournamentsWithMatches) {
            // Check if any match is ongoing or paused
            boolean hasOngoingMatches = tournamentRepository.hasOngoingMatches(tournament.getId());

            if (hasOngoingMatches) {
                // If any match is ongoing, set tournament to ONGOING
                if (tournament.getTournamentStatus() != ONGOING) {
                    tournament.setTournamentStatus(ONGOING);
                    tournamentRepository.save(tournament);
                }
            } else {
                // Check if all matches are completed or canceled
                boolean allFinished = tournamentRepository.allMatchesFinishedOrCanceled(tournament.getId());

                if (allFinished) {
                    // All matches are done, conclude the tournament
                    tournament.setTournamentStatus(CONCLUDED);
                    tournament.setActive(false);
                    tournamentRepository.save(tournament);
                } else {
                    // There are scheduled matches, keep as UPCOMING
                    if (tournament.getTournamentStatus() != UPCOMING) {
                        tournament.setTournamentStatus(UPCOMING);
                        tournamentRepository.save(tournament);
                    }
                }
            }
        }
    }

    @Override
    public void concludeTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        tournament.setTournamentStatus(CONCLUDED);
        tournament.setActive(false);
        tournamentRepository.save(tournament);
    }

    @Override
    public TournamentResponse getMostRecentTournament() {
        Tournament tournament = tournamentRepository.findTopByOrderByTournamentDateDesc();
        if (tournament == null) {
            throw new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return convertToDto(tournament);
    }


}
