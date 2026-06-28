package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "group_standing")
public class GroupStanding extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private RoundGroup group;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Builder.Default
    @Column(name = "matches_played", nullable = false)
    private Integer matchesPlayed = 0;

    @Builder.Default
    @Column(name = "wins", nullable = false)
    private Integer wins = 0;

    @Builder.Default
    @Column(name = "draws", nullable = false)
    private Integer draws = 0;

    @Builder.Default
    @Column(name = "losses", nullable = false)
    private Integer losses = 0;

    @Builder.Default
    @Column(name = "goals_for", nullable = false)
    private Integer goalsFor = 0;

    @Builder.Default
    @Column(name = "goals_against", nullable = false)
    private Integer goalsAgainst = 0;

    @Builder.Default
    @Column(name = "goal_difference", nullable = false)
    private Integer goalDifference = 0;

    @Builder.Default
    @Column(name = "points", nullable = false)
    private Integer points = 0;

    @Builder.Default
    @Column(name = "yellow_cards", nullable = false)
    private Integer yellowCards = 0;

    @Builder.Default
    @Column(name = "red_cards", nullable = false)
    private Integer redCards = 0;

    /**
     * Fair-play points using the UEFA deduction model (lower is better).
     * yellow = -1, second yellow (indirect red) = -3, direct red = -4,
     * yellow + direct red = -5.
     */
    @Builder.Default
    @Column(name = "fair_play_points", nullable = false)
    private Integer fairPlayPoints = 0;

    /**
     * Manually-recorded penalty-shootout tiebreak order for teams that are
     * level on every other criterion. Lower value ranks higher; null = unset.
     */
    @Column(name = "tiebreak_rank")
    private Integer tiebreakRank;

    @Column(name = "position")
    private Integer position;

    @Builder.Default
    @Column(name = "is_advanced")
    private Boolean isAdvanced = false;
}
