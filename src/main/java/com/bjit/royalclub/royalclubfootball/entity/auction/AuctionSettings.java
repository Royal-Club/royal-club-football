package com.bjit.royalclub.royalclubfootball.entity.auction;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.AuctionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "auction_settings")
public class AuctionSettings extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tournament_id", nullable = false, unique = true)
    private Tournament tournament;

    @Column(name = "team_budget", nullable = false)
    private Integer teamBudget;

    @Column(name = "min_squad_size", nullable = false)
    private Integer minSquadSize;

    @Column(name = "max_squad_size", nullable = false)
    private Integer maxSquadSize;

    @Column(name = "auction_timer_seconds", nullable = false)
    private Integer auctionTimerSeconds;

    @Column(name = "bid_increment", nullable = false)
    private Integer bidIncrement;

    @Column(name = "unsold_reauction_enabled", nullable = false)
    private Boolean unsoldReauctionEnabled;

    @Column(name = "timer_extension_seconds", nullable = false)
    private Integer timerExtensionSeconds;

    @Column(name = "extend_if_bid_within_last_seconds", nullable = false)
    private Integer extendIfBidWithinLastSeconds;

    @Column(name = "min_role_requirements", columnDefinition = "JSON")
    private String minRoleRequirements;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_status", nullable = false)
    private AuctionStatus auctionStatus;
}
