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
 * One row per reminder actually dispatched to a player for a tournament.
 * Used to cap the number of reminders per player and to audit what was sent.
 */
@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tournament_reminder_log")
public class TournamentReminderLog extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    /**
     * INVITE (sent once at tournament creation) or REMINDER (the D-2/D-1/match-day nudges).
     * Both occupy their calendar day for de-duplication; only REMINDER counts against the cap.
     */
    @Column(name = "reminder_type", nullable = false, length = 20)
    private String reminderType;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
