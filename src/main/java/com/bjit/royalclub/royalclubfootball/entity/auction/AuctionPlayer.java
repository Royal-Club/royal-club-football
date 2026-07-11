package com.bjit.royalclub.royalclubfootball.entity.auction;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerCategory;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerStatus;
import com.bjit.royalclub.royalclubfootball.enums.AuctionPlayerType;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "auction_players", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id", "player_id"})
})
public class AuctionPlayer extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(name = "player_type", nullable = false)
    private AuctionPlayerType playerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private AuctionPlayerCategory category;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    @Column(name = "current_bid")
    private Integer currentBid;

    @ManyToOne
    @JoinColumn(name = "current_highest_team_id")
    private Team currentHighestTeam;

    @ManyToOne
    @JoinColumn(name = "sold_to_team_id")
    private Team soldToTeam;

    @Column(name = "final_price")
    private Integer finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuctionPlayerStatus status;

    @Column(name = "auction_round", nullable = false)
    private Integer auctionRound;

    @Column(name = "player_rating")
    private BigDecimal playerRating;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Version
    @Column(name = "version")
    private Integer version;
}
