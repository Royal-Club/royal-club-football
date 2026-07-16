package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.MonthlyDuesReminderLog;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.repository.MonthlyDuesReminderLogRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nudges active players who have not paid their monthly club dues.
 *
 * <p>"Unpaid" means no {@code ac_collections} row covers the player for that month — the club has no
 * per-player expected amount, so any payment at all clears them.
 *
 * <p>Modelled on {@link TournamentReminderServiceImpl}: find who is due, filter by a per-player cap,
 * push through {@link NotificationService}, then log one row per send.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyDuesReminderServiceImpl implements MonthlyDuesReminderService {

    private static final String CHANNEL_PUSH = "PUSH";
    private static final DateTimeFormatter MONTH_DISPLAY = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * The club runs on Bangladesh time and the scheduler fires in Asia/Dhaka, but the JVM default is
     * UTC. Resolve "this month" in the club's zone so a run near midnight on the 1st or the 7th
     * cannot land on the wrong month.
     */
    private static final ZoneId CLUB_ZONE = ZoneId.of("Asia/Dhaka");

    private final PlayerRepository playerRepository;
    private final MonthlyDuesReminderLogRepository reminderLogRepository;
    private final NotificationService notificationService;

    @Value("${dues-reminders.max-per-month:4}")
    private int maxPerMonth;

    @Override
    @Transactional
    public int sendDueReminders() {
        return remindUnpaidForMonth(LocalDate.now(CLUB_ZONE));
    }

    @Override
    @Transactional
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

        // The 7th-10th cron bounds this to four runs, but the cap also protects against a retuned
        // cron or a job that fires twice in a day.
        List<Player> eligible = new ArrayList<>();
        for (Player player : unpaidPlayers) {
            int alreadySent = reminderLogRepository.countByPlayerIdAndMonthOfPayment(player.getId(), firstOfMonth);
            if (alreadySent < maxPerMonth) {
                eligible.add(player);
            }
        }

        if (eligible.isEmpty()) {
            log.info("Dues reminders for {}: {} player(s) unpaid but all have hit the {}-reminder cap.",
                    firstOfMonth.format(MONTH_KEY), unpaidPlayers.size(), maxPerMonth);
            return 0;
        }

        String title = "💰 Monthly payment due";
        String body = String.format("Your %s club payment is still pending. Tap to see your payments.",
                firstOfMonth.format(MONTH_DISPLAY));
        Map<String, String> data = Map.of(
                "type", "PAYMENT_REMINDER",
                "month", firstOfMonth.format(MONTH_KEY));

        notificationService.sendToPlayers(eligible, title, body, data);

        LocalDateTime sentAt = LocalDateTime.now();
        eligible.forEach(player -> reminderLogRepository.save(MonthlyDuesReminderLog.builder()
                .player(player)
                .monthOfPayment(firstOfMonth)
                .channel(CHANNEL_PUSH)
                .sentAt(sentAt)
                .build()));

        log.info("Dues reminders for {}: reminded {} of {} unpaid player(s).",
                firstOfMonth.format(MONTH_KEY), eligible.size(), unpaidPlayers.size());
        return eligible.size();
    }
}
