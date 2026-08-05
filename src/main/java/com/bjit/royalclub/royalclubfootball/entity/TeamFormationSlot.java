package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.enums.PositionGroup;
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

import java.math.BigDecimal;

/**
 * One place in a line-up: a pitch position for starters, a bench place
 * otherwise. Starters and substitutes share the {@code slotIndex} sequence,
 * starters first, so the order the captain chose is preserved on both sides.
 */
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_formation_slot")
public class TeamFormationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formation_id", nullable = false)
    private TeamFormation formation;

    /** Null while the captain has left the slot empty. */
    @ManyToOne
    @JoinColumn(name = "team_player_id")
    private TeamPlayer teamPlayer;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_group", nullable = false, length = 10)
    private PositionGroup positionGroup;

    @Column(name = "slot_label", length = 30)
    private String slotLabel;

    /** Percentage across the pitch box, 0 = left touchline. */
    @Column(name = "x", nullable = false, precision = 5, scale = 2)
    private BigDecimal x;

    /** Percentage down the pitch box, 100 = the team's own goal line. */
    @Column(name = "y", nullable = false, precision = 5, scale = 2)
    private BigDecimal y;

    @Column(name = "is_starter", nullable = false)
    private Boolean isStarter;
}
