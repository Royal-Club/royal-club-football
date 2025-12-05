package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.AdvancementRuleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "advancement_rule")
public class AdvancementRule extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_round_id", nullable = false)
    private TournamentRound sourceRound;

    @ManyToOne
    @JoinColumn(name = "source_group_id")
    private RoundGroup sourceGroup;

    @ManyToOne
    @JoinColumn(name = "target_round_id", nullable = false)
    private TournamentRound targetRound;

    @ManyToOne
    @JoinColumn(name = "target_group_id")
    private RoundGroup targetGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 50)
    private AdvancementRuleType ruleType;

    @Column(name = "rule_config", columnDefinition = "TEXT", nullable = false)
    private String ruleConfig;

    @Column(name = "priority_order")
    private Integer priorityOrder;
}
