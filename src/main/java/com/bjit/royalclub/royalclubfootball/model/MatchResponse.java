package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {

    private Long id;
    private Long tournamentId;
    private String tournamentName;
    private Long homeTeamId;
    private String homeTeamName;
    private Long awayTeamId;
    private String awayTeamName;
    private Long venueId;
    private String venueName;
    private LocalDateTime matchDate;
    private String matchStatus;
    private Integer matchOrder;
    private Integer round;  // Legacy round field
    private Integer roundNumber;  // Round number from TournamentRound (for manual fixture system)
    private String groupName;
    private Integer homeTeamScore;
    private Integer awayTeamScore;
    private Integer matchDurationMinutes;
    private Integer elapsedTimeSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
