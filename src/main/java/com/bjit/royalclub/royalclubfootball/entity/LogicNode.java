package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.LogicNodeType;
import jakarta.persistence.*;
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
@Table(name = "logic_node")
public class LogicNode extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_type", nullable = false, length = 50)
    private LogicNodeType nodeType;

    /**
     * Source round (if advancing from a round)
     * Nullable - can be from group instead
     */
    @ManyToOne
    @JoinColumn(name = "source_round_id")
    private TournamentRound sourceRound;

    /**
     * Source group (if advancing from a group)
     * Nullable - can be from round instead
     */
    @ManyToOne
    @JoinColumn(name = "source_group_id")
    private RoundGroup sourceGroup;

    /**
     * Target round where teams will advance to
     * Required - teams always advance to a round
     */
    @ManyToOne
    @JoinColumn(name = "target_round_id", nullable = false)
    private TournamentRound targetRound;

    /**
     * JSON configuration for the rule
     * Stores rule details like topN, tieBreakerRules, etc.
     */
    @Column(name = "rule_config", columnDefinition = "TEXT")
    private String ruleConfig;

    /**
     * Priority order for execution when multiple logic nodes exist
     * Lower number = higher priority
     */
    @Column(name = "priority_order")
    private Integer priorityOrder;

    /**
     * Whether this logic node is active
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Whether to auto-execute when source completes
     */
    @Column(name = "auto_execute", nullable = false)
    @Builder.Default
    private Boolean autoExecute = true;

    /**
     * Execution count (how many times this node has been executed)
     */
    @Column(name = "execution_count", nullable = false)
    @Builder.Default
    private Integer executionCount = 0;

    /**
     * Last execution timestamp
     */
    @Column(name = "last_executed_at")
    private java.time.LocalDateTime lastExecutedAt;
}

