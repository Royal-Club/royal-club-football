package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.TournamentRoundRequest;
import com.bjit.royalclub.royalclubfootball.model.TournamentRoundResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentStructureResponse;
import com.bjit.royalclub.royalclubfootball.model.AdvancedTeamsResponse;
import com.bjit.royalclub.royalclubfootball.model.RoundCompletionRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamAssignmentRequest;
import com.bjit.royalclub.royalclubfootball.model.RoundMatchGenerationRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface TournamentRoundService {

    @Transactional
    TournamentRoundResponse createRound(TournamentRoundRequest request);

    @Transactional
    TournamentRoundResponse updateRound(Long roundId, TournamentRoundRequest request);

    @Transactional
    void deleteRound(Long roundId);

    TournamentRoundResponse getRoundById(Long roundId);

    List<TournamentRoundResponse> getRoundsByTournamentId(Long tournamentId);

    TournamentStructureResponse getTournamentStructure(Long tournamentId);

    @Transactional
    AdvancedTeamsResponse completeRound(RoundCompletionRequest request);

    @Transactional
    TournamentRoundResponse startRound(Long roundId);

    TournamentRoundResponse getNextRound(Long tournamentId, Integer currentSequenceOrder);

    TournamentRoundResponse getPreviousRound(Long tournamentId, Integer currentSequenceOrder);

    @Transactional
    void assignTeamsToRound(Long roundId, TeamAssignmentRequest request);

    @Transactional
    List<MatchResponse> generateRoundMatches(Long roundId, RoundMatchGenerationRequest request);
}
