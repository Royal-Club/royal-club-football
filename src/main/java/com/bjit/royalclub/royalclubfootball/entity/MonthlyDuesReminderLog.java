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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One row per monthly-dues reminder actually dispatched to a player for a given month.
 * Used to cap the number of reminders per player per month and to audit what was sent.
 *
 * @see com.bjit.royalclub.royalclubfootball.entity.TournamentReminderLog for the RSVP equivalent.
 */
@Entity
@Setter
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monthly_dues_reminder_log")
public class MonthlyDuesReminderLog extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /** First day of the month the dues are owed for, matching AcCollection.monthOfPayment. */
    @Column(name = "month_of_payment", nullable = false)
    private LocalDate monthOfPayment;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
