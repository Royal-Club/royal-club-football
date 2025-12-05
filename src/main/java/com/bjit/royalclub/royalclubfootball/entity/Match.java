package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.MatchStatus;
import com.bjit.royalclub.royalclubfootball.enums.MatchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`match`")
public class Match extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    // New fields for manual fixture system
    @ManyToOne
    @JoinColumn(name = "round_id")
    private TournamentRound round;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private RoundGroup group;

    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status", nullable = false)
    private MatchStatus matchStatus;

    @Column(name = "match_order")
    private Integer matchOrder;

    @Column(name = "round")
    private Integer legacyRound;  // Legacy field for old fixture system

    @Column(name = "group_name")
    private String groupName;

    // New fields for manual fixture system
    @Column(name = "is_placeholder_match", nullable = false)
    private Boolean isPlaceholderMatch = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", length = 50)
    private MatchType matchType;

    @Column(name = "series_number")
    private Integer seriesNumber;

    @Column(name = "bracket_position", length = 50)
    private String bracketPosition;

    @Column(name = "home_team_score", nullable = false)
    private Integer homeTeamScore;

    @Column(name = "away_team_score", nullable = false)
    private Integer awayTeamScore;

    @Column(name = "match_duration_minutes")
    private Integer matchDurationMinutes;

    @Column(name = "elapsed_time_seconds", nullable = false)
    private Integer elapsedTimeSeconds;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchEvent> matchEvents;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchStatistics> matchStatistics;
}
