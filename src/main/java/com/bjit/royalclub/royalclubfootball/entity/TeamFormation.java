package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A line-up a captain has laid out on the pitch. {@code match} being null marks
 * the team's default formation; every other row belongs to one specific match.
 */
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "team_formation")
public class TeamFormation extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Null for the team's default formation. */
    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(name = "preset_name", nullable = false, length = 30)
    private String presetName;

    @Column(name = "team_size", nullable = false)
    private Integer teamSize;

    @Column(name = "notes", length = 500)
    private String notes;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("slotIndex ASC")
    @lombok.Builder.Default
    private List<TeamFormationSlot> slots = new ArrayList<>();
}
