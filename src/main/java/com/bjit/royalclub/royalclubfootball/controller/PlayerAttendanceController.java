package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.PlayerAttendanceResponse;
import com.bjit.royalclub.royalclubfootball.service.PlayerAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/player-attendance")
public class PlayerAttendanceController {

    private final PlayerAttendanceService playerAttendanceService;

    /**
     * Attendance and reliability for every player.
     *
     * @param year       restrict to one calendar year (optional, default all time)
     * @param activeOnly exclude deactivated players (default true)
     */
    @GetMapping
    public ResponseEntity<Object> getAttendanceReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly) {
        List<PlayerAttendanceResponse> report =
                playerAttendanceService.getAttendanceReport(year, activeOnly);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, report);
    }
}
