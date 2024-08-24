package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.model.TournamentRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentUpdateRequest;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TournamentService {

    Tournament getNextUpcomingTournament();

    @Transactional
    TournamentResponse saveTournament(TournamentRequest tournamentRequest);

    TournamentResponse getTournamentById(Long id);

    void updateTournamentStatus(Long id, boolean active);

    List<TournamentResponse> getAllTournament();

    TournamentResponse updateTournament(Long id, TournamentUpdateRequest updateTournamentRequest);
}
