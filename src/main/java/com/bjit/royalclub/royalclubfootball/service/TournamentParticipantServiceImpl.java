package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.TournamentParticipant;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.TournamentParticipantRequest;
import com.bjit.royalclub.royalclubfootball.repository.TournamentParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TournamentParticipantServiceImpl implements TournamentParticipantService {

    private final TournamentParticipantRepository tournamentParticipantRepository;
    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;

    @Override
    public void saveTournamentParticipant(TournamentParticipantRequest tournamentParticipantRequest) {
        Tournament tournament = tournamentRepository.findById(tournamentParticipantRequest.getTournamentScheduleId())
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        Player player = playerRepository.findById(tournamentParticipantRequest.getPlayerId())
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        TournamentParticipant tournamentParticipant = TournamentParticipant.builder()
                .id(tournamentParticipantRequest.getId())
                .tournament(tournament)
                .player(player)
                .participationStatus(tournamentParticipantRequest.isParticipationStatus())
                .isActive(tournamentParticipantRequest.isActive())
                .createdDate(LocalDateTime.now())
                .updatedDate(tournamentParticipantRequest.getUpdatedDate())
                .build();
        tournamentParticipantRepository.save(tournamentParticipant);
    }

}
