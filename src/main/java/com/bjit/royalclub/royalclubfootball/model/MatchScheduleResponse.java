package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class MatchScheduleResponse {
    private Long id;
    private LocalDate dateTime;
    private String venueName;
}
