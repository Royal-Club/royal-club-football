package com.bjit.royalclub.royalclubfootball.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
public class TournamentResponse {
    private Long id;
    private String tournamentName;
    private LocalDateTime tournamentDate;
    private String venueName;

    private boolean activeStatus;
}
