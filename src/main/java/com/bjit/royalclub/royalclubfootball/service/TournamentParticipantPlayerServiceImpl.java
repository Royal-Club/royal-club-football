package com.bjit.royalclub.royalclubfootball.service;

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

    private final TournamentParticipantPlayerRepository repository;

    @Override
    public List<TournamentWithPlayersResponse> getAllTournamentsWithPlayers() {
        List<TournamentParticipantPlayer> participants = repository.findAll();

        // Group by tournament and transform into the desired format
        Map<Long, List<TournamentParticipantPlayer>> groupedByTournament = participants.stream()
                .collect(Collectors.groupingBy(TournamentParticipantPlayer::getTournamentId));

        return groupedByTournament.values().stream()
                .map(tournamentParticipantPlayers -> {
                    TournamentParticipantPlayer firstEntry = tournamentParticipantPlayers.get(0);
                    List<PlayerParticipationResponse> players = tournamentParticipantPlayers.stream()
                            .map(player -> new PlayerParticipationResponse(player.getPlayerId(),
                                    player.getParticipationStatus()))
                            .toList();
                    return new TournamentWithPlayersResponse(
                            firstEntry.getTournamentId(),
                            firstEntry.getTournamentName(),
                            firstEntry.getTournamentDate(),
                            players
                    );
                })
                .toList();
    }
}
