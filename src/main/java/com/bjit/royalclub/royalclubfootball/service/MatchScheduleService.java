package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.MatchScheduleRequest;
import com.bjit.royalclub.royalclubfootball.model.MatchScheduleResponse;

import java.util.List;

public interface MatchScheduleService {

    List<MatchScheduleResponse> getUpcomingMatches();

    MatchScheduleResponse saveMatch(MatchScheduleRequest matchScheduleRequest);
}
