package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.PlayerAttendanceResponse;

import java.util.List;

public interface PlayerAttendanceService {

    /**
     * Club-wide attendance and reliability, one row per player.
     *
     * @param year       restrict to a single calendar year, or null for all time
     * @param activeOnly exclude deactivated players when true
     */
    List<PlayerAttendanceResponse> getAttendanceReport(Integer year, boolean activeOnly);
}
