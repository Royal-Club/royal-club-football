package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.RoundStatus;
import com.bjit.royalclub.royalclubfootball.enums.RoundType;
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
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tournament_round")
public class TournamentRound extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "round_name", nullable = false, length = 100)
    private String roundName;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false, length = 50)
    private RoundType roundType;

    @Column(name = "advancement_rule", columnDefinition = "TEXT")
    private String advancementRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoundStatus status;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundGroup> groups = new ArrayList<>();

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundTeam> teams = new ArrayList<>();

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL)
    private List<Match> matches = new ArrayList<>();

    @OneToMany(mappedBy = "sourceRound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdvancementRule> sourceAdvancementRules = new ArrayList<>();

    @OneToMany(mappedBy = "targetRound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdvancementRule> targetAdvancementRules = new ArrayList<>();

    @OneToMany(mappedBy = "sourceRound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LogicNode> sourceLogicNodes = new ArrayList<>();

    @OneToMany(mappedBy = "targetRound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LogicNode> targetLogicNodes = new ArrayList<>();
}
