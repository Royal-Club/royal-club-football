package com.bjit.royalclub.royalclubfootball.entity.auction;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "team_budgets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id", "team_id"})
})
public class TeamBudget extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Player owner;

    @Column(name = "total_budget", nullable = false)
    private Integer totalBudget;

    @Column(name = "remaining_budget", nullable = false)
    private Integer remainingBudget;

    @Column(name = "total_spent", nullable = false)
    private Integer totalSpent;

    @Column(name = "players_bought", nullable = false)
    private Integer playersBought;

    @Version
    @Column(name = "version")
    private Integer version;
}
