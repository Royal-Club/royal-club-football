package com.bjit.royalclub.royalclubfootball.entity.auction;

import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.AuctionSessionStatus;
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

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "auction_sessions")
public class AuctionSession extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuctionSessionStatus status;

    @ManyToOne
    @JoinColumn(name = "current_auction_player_id")
    private AuctionPlayer currentAuctionPlayer;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "current_timer_ends_at")
    private LocalDateTime currentTimerEndsAt;
}
