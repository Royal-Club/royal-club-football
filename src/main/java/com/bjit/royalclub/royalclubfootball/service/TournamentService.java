package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PaginatedTournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentListResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentUpdateRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TournamentService {

    @Transactional
    TournamentResponse saveTournament(TournamentRequest tournamentRequest);

    TournamentResponse getTournamentById(Long id);

    @Transactional
    void updateTournamentStatus(Long id, boolean active);

    PaginatedTournamentResponse getAllTournament(int offSet, int pageSize,
                                                 String sortedBy, String sortDirection, String searchColumn, String searchValue);

    @Transactional
    TournamentResponse updateTournament(Long id, TournamentUpdateRequest updateTournamentRequest);

    @Transactional
    void updateTournamentStatuses();

    @Transactional
    void concludeTournament(Long tournamentId);

    TournamentResponse getMostRecentTournament();

    /**
     * Get list of tournaments (id, name, and tournamentDate) ordered by tournament date descending
     * @param year Optional year filter in format "YYYY" (e.g., "2025")
     * @return List of tournaments with id, name, and tournamentDate
     */
    List<TournamentListResponse> getTournamentList(String year);

    /**
     * Get list of unique years where tournaments exist
     * Returns years in format "YYYY" (e.g., "2025", "2024")
     * Ordered by year descending (newest first)
     * @return List of year strings
     */
    List<String> getTournamentSessions();

}
