package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.TournamentRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentUpdateRequest;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface TournamentService {

    @Transactional
    TournamentResponse saveTournament(TournamentRequest tournamentRequest);

    TournamentResponse getTournamentById(Long id);

    @Transactional
    void updateTournamentStatus(Long id, boolean active);

    List<TournamentResponse> getAllTournament(int offSet, int  pageSize,
                                            String sortedBy, String sortDirection);

    @Transactional
    TournamentResponse updateTournament(Long id, TournamentUpdateRequest updateTournamentRequest);
}
