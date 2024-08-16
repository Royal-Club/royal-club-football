package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.MatchParticipant;
import com.bjit.royalclub.royalclubfootball.entity.MatchSchedule;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.MatchScheduleServiceException;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.MatchParticipantRequest;
import com.bjit.royalclub.royalclubfootball.repository.MatchParticipantRepository;
import com.bjit.royalclub.royalclubfootball.repository.MatchScheduleRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.MATCH_SCHEDULE_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MatchParticipantServiceImpl implements MatchParticipantService {

    private final MatchParticipantRepository matchParticipantRepository;
    private final MatchScheduleRepository matchScheduleRepository;
    private final PlayerRepository playerRepository;

    @Override
    public void createMatchParticipant(MatchParticipantRequest matchParticipantRequest) {
        MatchSchedule matchSchedule = matchScheduleRepository.findById(matchParticipantRequest.getMatchScheduleId())
                .orElseThrow(() -> new MatchScheduleServiceException(MATCH_SCHEDULE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        Player player = playerRepository.findById(matchParticipantRequest.getPlayerId())
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.CONFLICT));

        MatchParticipant matchParticipant = MatchParticipant.builder()
                .id(matchParticipantRequest.getId())
                .matchSchedule(matchSchedule)
                .player(player)
                .participationStatus(matchParticipantRequest.isParticipationStatus())
                .isActive(matchParticipantRequest.isActive())
                .createdDate(LocalDateTime.now())
                .updatedDate(matchParticipantRequest.getUpdatedDate())
                .build();
        matchParticipantRepository.save(matchParticipant);
    }

}
