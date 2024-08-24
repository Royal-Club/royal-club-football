package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipantPlayer;
import com.bjit.royalclub.royalclubfootball.model.PlayerParticipationResponse;
import com.bjit.royalclub.royalclubfootball.model.TournamentWithPlayersResponse;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentParticipantPlayerServiceImpl implements TournamentParticipantPlayerService {

    private final TournamentParticipantPlayerRepository participantPlayerRepository;
    private final TournamentService tournamentService;

    @Override
    public List<TournamentWithPlayersResponse> getAllTournamentsWithPlayers() {
        List<TournamentParticipantPlayer> participants = participantPlayerRepository.findAll();

        // Group by tournament and transform into the desired format
        Map<Long, List<TournamentParticipantPlayer>> groupedByTournament = participants.stream()
                .collect(Collectors.groupingBy(TournamentParticipantPlayer::getTournamentId));

        return groupedByTournament.values().stream()
                .map(this::buildTournamentWithPlayersResponse)
                .toList();
    }

    private TournamentWithPlayersResponse buildTournamentWithPlayersResponse(List<TournamentParticipantPlayer> tournamentParticipantPlayers) {
        TournamentParticipantPlayer firstEntry = tournamentParticipantPlayers.get(0);
        List<PlayerParticipationResponse> players = tournamentParticipantPlayers.stream()
                .map(player -> PlayerParticipationResponse.builder()
                        .playerId(player.getPlayerId())
                        .playerName(player.getPlayerName())
                        .employeeId(player.getPlayerEmployeeId())
                        .participationStatus(player.getParticipationStatus())
                        .build())
                .toList();
        return new TournamentWithPlayersResponse(
                firstEntry.getTournamentId(),
                firstEntry.getTournamentName(),
                firstEntry.getTournamentDate(),
                players
        );
    }

    @Override
    public TournamentWithPlayersResponse findNextSingleTournamentWithPlayers() {
        /*Here only get a single upcoming tournament. However, tournament is available then always the
        TournamentParticipantPlayer exists*/
        Tournament tournament = tournamentService.getNextUpcomingTournament();
        List<TournamentParticipantPlayer> participants = participantPlayerRepository.findAllByTournamentId(tournament.getId());
        return buildTournamentWithPlayersResponse(participants);
    }
}
