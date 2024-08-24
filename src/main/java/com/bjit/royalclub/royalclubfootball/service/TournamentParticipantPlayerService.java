package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.TournamentWithPlayersResponse;

import java.util.List;

public interface TournamentParticipantPlayerService {
    List<TournamentWithPlayersResponse> getAllTournamentsWithPlayers();

    TournamentWithPlayersResponse findNextSingleTournamentWithPlayers();
}
