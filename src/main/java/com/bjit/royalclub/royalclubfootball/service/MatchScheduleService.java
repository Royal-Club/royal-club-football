package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.MatchScheduleRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchScheduleResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface MatchScheduleService {

    List<MatchScheduleResponse> getUpcomingMatches();

    @Transactional
    MatchScheduleResponse saveMatch(MatchScheduleRequest matchScheduleRequest);
}
