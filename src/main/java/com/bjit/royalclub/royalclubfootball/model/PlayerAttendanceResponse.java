package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A player's turn-up record across tournaments: how often they were asked, how
 * often they answered, and how often they actually took the pitch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAttendanceResponse {

    private Long playerId;
    private String playerName;
    private String position;
    private boolean active;

    /** Tournaments counted against this player (those held since they joined). */
    private Integer eligibleTournaments;

    /** Answered "yes" on the RSVP. */
    private Integer confirmed;

    /** Answered "no" on the RSVP. */
    private Integer declined;

    /** Never answered either way. */
    private Integer noResponse;

    /** Tournaments where the player was actually assigned to a team. */
    private Integer played;

    /** Confirmed but never made a team sheet. */
    private Integer confirmedButNotPlayed;

    /** played / eligibleTournaments, as a percentage. */
    private Double attendanceRate;

    /** (confirmed + declined) / eligibleTournaments, as a percentage. */
    private Double responseRate;

    /** played / confirmed, as a percentage — how often a "yes" turns into a game. */
    private Double reliabilityRate;

    /** Tournaments played in a row, counting back from the most recent one. */
    private Integer currentStreak;

    /** Longest run of consecutive tournaments played. */
    private Integer longestStreak;

    /** Consecutive most recent tournaments missed. */
    private Integer currentAbsenceStreak;

    private LocalDateTime lastPlayedDate;

    /** Date of the earliest tournament counted for this player. */
    private LocalDateTime firstCountedDate;
}
