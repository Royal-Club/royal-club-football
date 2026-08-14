package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Records that a player has been told they are in a published line-up.
 * <p>
 * The ledger is what makes publishing idempotent and incremental. Publishing asks which placed
 * players have no row yet and notifies only those, so pressing the button twice is harmless, and
 * swapping a player in and republishing reaches the replacement without disturbing anyone else.
 *
 * @see com.bjit.royalclub.royalclubfootball.entity.TournamentReminderLog for the same
 * send-log-as-ledger pattern.
 */
@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lineup_notification_log")
public class LineupNotificationLog extends AuditBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formation_id", nullable = false)
    private TeamFormation formation;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "notified_at", nullable = false)
    private LocalDateTime notifiedAt;
}
