package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PaginatedTournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentUpdateRequest;
import jakarta.transaction.Transactional;

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
    void deactivateAndConcludePastTournaments();
}
