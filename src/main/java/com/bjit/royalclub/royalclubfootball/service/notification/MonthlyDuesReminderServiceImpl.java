package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.MonthlyDuesReminderLog;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.repository.MonthlyDuesReminderLogRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Nudges active players who have not paid their monthly club dues, on push and by email.
 *
 * <p>"Unpaid" means no {@code ac_collections} row covers the player for that month — the club has no
 * per-player expected amount, so any payment at all clears them.
 *
 * <p>Current month only: an unpaid month stops being chased the moment the next month begins.
 *
 * <p>Modelled on {@link TournamentReminderServiceImpl}: find who is due, filter per channel by a cap
 * and a one-contact-per-day rule, dispatch, then log one row per message actually sent. It also
 * follows that class in staying out of a transaction, so the sequential SMTP loop never holds a
 * pooled database connection - see there for the reasoning.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyDuesReminderServiceImpl implements MonthlyDuesReminderService {

    private static final String CHANNEL_PUSH = "PUSH";
    private static final String CHANNEL_EMAIL = "EMAIL";
    private static final DateTimeFormatter MONTH_DISPLAY = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PlayerRepository playerRepository;
    private final MonthlyDuesReminderLogRepository reminderLogRepository;
    private final NotificationService notificationService;
    private final DuesEmailService duesEmailService;

    @Value("${dues-reminders.max-per-month:3}")
    private int maxPerMonth;

    @Value("${dues-reminders.email-enabled:true}")
    private boolean emailEnabled;

    /**
     * The club runs on Bangladesh time and the scheduler fires in Asia/Dhaka, but the JVM default is
     * UTC. Resolve "this month" in the club's zone so a run near midnight cannot land on the wrong month.
     */
    @Value("${dues-reminders.zone:Asia/Dhaka}")
    private String clubZone;

    @Override
    public int sendDueReminders() {
        return remindUnpaidForMonth(LocalDate.now(ZoneId.of(clubZone)));
    }

    @Override
    public int remindUnpaidForMonth(LocalDate month) {
        LocalDate firstOfMonth = month.withDayOfMonth(1);
        LocalDate lastOfMonth = month.withDayOfMonth(month.lengthOfMonth());

        List<Player> unpaidPlayers =
                playerRepository.findActivePlayersWithoutCollectionForMonth(firstOfMonth, lastOfMonth);

        if (unpaidPlayers.isEmpty()) {
            log.info("Dues reminders for {}: every active player has paid; nothing to send.",
                    firstOfMonth.format(MONTH_KEY));
            return 0;
        }

        int dispatched = dispatch(unpaidPlayers, firstOfMonth, CHANNEL_PUSH);

        if (emailEnabled) {
            dispatched += dispatch(unpaidPlayers, firstOfMonth, CHANNEL_EMAIL);
        } else {
            log.info("Dues reminders for {}: email is disabled club-wide; push only.",
                    firstOfMonth.format(MONTH_KEY));
        }
        return dispatched;
    }

    private int dispatch(List<Player> unpaidPlayers, LocalDate firstOfMonth, String channel) {
        List<Player> eligible = filterEligible(unpaidPlayers, firstOfMonth, channel);

        if (eligible.isEmpty()) {
            log.info("Dues reminders for {}: no players eligible for a {} reminder today.",
                    firstOfMonth.format(MONTH_KEY), channel);
            return 0;
        }

        List<Player> delivered;
        if (CHANNEL_EMAIL.equals(channel)) {
            // Returns only the recipients the mail server accepted, so a bounce is never logged as
            // sent and the player stays eligible on the next run day.
            delivered = duesEmailService.sendDuesEmails(eligible, firstOfMonth);
        } else {
            notificationService.sendToPlayers(eligible, "💰 Monthly payment due",
                    String.format("Your %s club payment is still pending. Tap to see your payments.",
                            firstOfMonth.format(MONTH_DISPLAY)),
                    Map.of("type", "PAYMENT_REMINDER", "month", firstOfMonth.format(MONTH_KEY)));
            // FCM multicast reports per-token results, not per-player, so treat the batch as sent.
            delivered = eligible;
        }

        recordLogs(delivered, firstOfMonth, channel);

        log.info("Dues reminders for {}: {} reminder sent to {} of {} unpaid player(s).",
                firstOfMonth.format(MONTH_KEY), channel, delivered.size(), unpaidPlayers.size());
        return delivered.size();
    }

    private List<Player> filterEligible(List<Player> unpaidPlayers, LocalDate firstOfMonth, String channel) {
        ZoneId zone = ZoneId.of(clubZone);
        LocalDate today = LocalDate.now(zone);
        Set<Long> contactedToday = new HashSet<>(reminderLogRepository.findPlayerIdsContactedBetween(
                firstOfMonth, channel,
                startOfDayInUtc(today, zone), startOfDayInUtc(today.plusDays(1), zone)));

        List<Long> playerIds = unpaidPlayers.stream().map(Player::getId).toList();
        Map<Long, Long> reminderCounts = new HashMap<>();
        reminderLogRepository.countByPlayersMonthAndChannel(playerIds, firstOfMonth, channel)
                .forEach(row -> reminderCounts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue()));

        List<Player> eligible = new ArrayList<>();
        for (Player player : unpaidPlayers) {
            if (contactedToday.contains(player.getId())) {
                continue;
            }
            // The 5th/10th/15th cron bounds this to three runs, but the cap also protects against a
            // retuned cron or a job that fires twice in a day.
            if (reminderCounts.getOrDefault(player.getId(), 0L) >= maxPerMonth) {
                continue;
            }
            eligible.add(player);
        }
        return eligible;
    }

    private void recordLogs(List<Player> delivered, LocalDate firstOfMonth, String channel) {
        if (delivered.isEmpty()) {
            return;
        }
        LocalDateTime sentAt = LocalDateTime.now(ZoneOffset.UTC);
        List<MonthlyDuesReminderLog> logs = new ArrayList<>();
        for (Player player : delivered) {
            logs.add(MonthlyDuesReminderLog.builder()
                    .player(player)
                    .monthOfPayment(firstOfMonth)
                    .channel(channel)
                    .sentAt(sentAt)
                    .build());
        }
        reminderLogRepository.saveAll(logs);
    }

    /**
     * Midnight of a {@code zone} calendar day, expressed on the UTC timeline the stored timestamps
     * use. Comparing a Dhaka day boundary against UTC values directly would drift six hours.
     */
    private LocalDateTime startOfDayInUtc(LocalDate day, ZoneId zone) {
        return day.atStartOfDay(zone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}
