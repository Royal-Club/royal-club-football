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
@Table(name = "match_statistics")
public class MatchStatistics extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "goals_scored", nullable = false)
    private Integer goalsScored;

    @Column(name = "assists", nullable = false)
    private Integer assists;

    @Column(name = "red_cards", nullable = false)
    private Integer redCards;

    @Column(name = "yellow_cards", nullable = false)
    private Integer yellowCards;

    @Column(name = "substitution_in", nullable = false)
    private Integer substitutionIn;

    @Column(name = "substitution_out", nullable = false)
    private Integer substitutionOut;

    @Column(name = "minutes_played", nullable = false)
    private Integer minutesPlayed;
}
