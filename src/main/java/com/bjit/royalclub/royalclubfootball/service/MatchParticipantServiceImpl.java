package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.MatchParticipant;
import com.bjit.royalclub.royalclubfootball.entity.MatchSchedule;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.model.MatchParticipantRequest;
import com.bjit.royalclub.royalclubfootball.repository.MatchParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.MatchScheduleRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MatchParticipantServiceImpl implements MatchParticipantService {

    private final MatchParticipantRepository matchParticipantRepository;
    private final MatchScheduleRepository matchScheduleRepository;
    private final PlayerRepository playerRepository;

    @Override
    public void createMatchParticipant(MatchParticipantRequest matchParticipantRequest) {
        MatchParticipant matchParticipant = convertToEntity(matchParticipantRequest);
        matchParticipantRepository.save(matchParticipant);
    }

    private MatchParticipant convertToEntity(MatchParticipantRequest participantRequest) {
        /*TODO ("Need to provide custom Exception")*/
        MatchSchedule matchSchedule = matchScheduleRepository.findById(participantRequest.getMatchScheduleId())
                .orElseThrow(() -> new RuntimeException("Match Schedule not found"));
        /*TODO ("Need to provide custom Exception")*/
        Player player = playerRepository.findById(participantRequest.getPlayerId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        return MatchParticipant.builder()
                .id(participantRequest.getId())
                .matchSchedule(matchSchedule)
                .player(player)
                .participationStatus(participantRequest.isParticipationStatus())
                .isActive(participantRequest.isActive())
                .createdDate(LocalDateTime.now())
                .updatedDate(participantRequest.getUpdatedDate())
                .build();
    }
}
