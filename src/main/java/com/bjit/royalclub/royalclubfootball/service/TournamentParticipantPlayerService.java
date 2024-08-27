package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.TournamentWithPlayersResponse;

public interface TournamentParticipantPlayerService {
    TournamentWithPlayersResponse getNextTournamentForParticipation(Long tournamentId);
}
