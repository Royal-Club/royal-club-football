package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PlayerParticipationResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentParticipantRequest;

import java.util.List;

public interface TournamentParticipantService {
    void updateTournamentParticipant(TournamentParticipantRequest tournamentParticipantRequest);

    List<PlayerParticipationResponse> playersToBeSelectedForTeam(Long tournamentId);
}
