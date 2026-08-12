package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentReminderLog;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentReminderLogRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.enums.TournamentStatus.UPCOMING;

/**
 * Drives the RSVP nudges: one contact per player per channel per calendar day, on D-2, D-1 and match
 * day, stopping as soon as the player answers Yes or No.
 * <p>
 * The entry points are deliberately not transactional. Each run is three phases - read who is
 * eligible, send the messages, record what was delivered - and the middle phase is a sequential SMTP
 * loop, one round trip per player. Wrapping the whole run in a transaction pinned a single pooled
 * connection for the entire loop, which on a 30-player squad is minutes. The phases stand on their
 * own: the reads are plain queries and {@code saveAll} in {@link #recordLogs} carries its own
 * transaction, so a batch of log rows is still written atomically.
 * <p>
 * The narrower boundary is also safer. Previously a failure late in a run rolled back the log rows
 * for players who had already been emailed, so they were mailed again on the next run.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentReminderServiceImpl implements TournamentReminderService {

    private static final String CHANNEL_PUSH = "PUSH";
    private static final String CHANNEL_EMAIL = "EMAIL";
    private static final String TYPE_INVITE = "INVITE";
    private static final String TYPE_REMINDER = "REMINDER";
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM 'at' hh:mm a");

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TournamentReminderLogRepository reminderLogRepository;
    private final NotificationService notificationService;
    private final RsvpEmailService rsvpEmailService;

    @Value("${reminders.lead-days:2}")
    private int leadDays;

    @Value("${reminders.max-per-player:3}")
    private int maxPerPlayer;

    @Value("${reminders.zone:Asia/Dhaka}")
    private String reminderZone;

    @Override
    public int sendDueReminders() {
        ZoneId zone = ZoneId.of(reminderZone);
        LocalDate today = LocalDate.now(zone);
        LocalDateTime now = utcNow();

        // Calendar-day offsets, not a rolling hour window: a 48h window run at 09:00 would miss a
        // D-2 tournament that kicks off in the afternoon.
        LocalDateTime windowStart = startOfDayInUtc(today, zone);
        // The repository query uses BETWEEN, which is inclusive at both ends, so stop a second short
        // of the next day's midnight rather than reaching into D-3.
        LocalDateTime windowEnd = startOfDayInUtc(today.plusDays(leadDays + 1L), zone).minusSeconds(1);

        List<Tournament> dueTournaments = tournamentRepository.findUpcomingWithinWindow(windowStart, windowEnd);

        if (dueTournaments.isEmpty()) {
            log.info("No tournaments within the next {} day(s); no reminders to send.", leadDays);
            return 0;
        }

        int total = 0;
        for (Tournament tournament : dueTournaments) {
            // Match-day guard: the window opens at midnight, so a tournament that already kicked off
            // this morning would otherwise still be picked up.
            if (!tournament.getTournamentDate().isAfter(now)) {
                log.info("Tournament '{}' ({}) has already started today; skipping.",
                        tournament.getName(), tournament.getId());
                continue;
            }
            total += contactPendingPlayers(tournament, TYPE_REMINDER, zone);
        }
        log.info("Reminder run complete: {} message(s) dispatched across {} tournament(s).",
                total, dueTournaments.size());
        return total;
    }

    @Override
    public int remindForTournament(Long tournamentId) {
        ZoneId zone = ZoneId.of(reminderZone);
        Tournament tournament = loadRemindableTournament(tournamentId, zone);
        return tournament == null ? 0 : contactPendingPlayers(tournament, TYPE_REMINDER, zone);
    }

    @Override
    public int sendInvitations(Long tournamentId) {
        ZoneId zone = ZoneId.of(reminderZone);
        Tournament tournament = loadRemindableTournament(tournamentId, zone);
        return tournament == null ? 0 : contactPendingPlayers(tournament, TYPE_INVITE, zone);
    }

    /**
     * @return the tournament, or null when it is cancelled, concluded or already under way - all cases
     * where nobody should be nudged any further.
     */
    private Tournament loadRemindableTournament(Long tournamentId, ZoneId zone) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (!tournament.isActive() || tournament.getTournamentStatus() != UPCOMING) {
            log.info("Tournament {} is cancelled or no longer upcoming; skipping.", tournamentId);
            return null;
        }
        if (!tournament.getTournamentDate().isAfter(utcNow())) {
            log.info("Tournament {} has already started; skipping.", tournamentId);
            return null;
        }
        return tournament;
    }

    /**
     * Contact every player who still owes a Yes/No, on push always and on email unless this tournament
     * has email switched off.
     */
    private int contactPendingPlayers(Tournament tournament, String reminderType, ZoneId zone) {
        List<Player> pending = playerRepository.findActivePlayersWithoutParticipation(tournament.getId());

        if (pending.isEmpty()) {
            log.info("Tournament '{}' ({}): every active player has already answered.",
                    tournament.getName(), tournament.getId());
            return 0;
        }

        int dispatched = dispatch(tournament, pending, CHANNEL_PUSH, reminderType, zone);

        if (tournament.isEmailNotificationEnabled()) {
            dispatched += dispatch(tournament, pending, CHANNEL_EMAIL, reminderType, zone);
        } else {
            log.info("Tournament '{}' ({}): email notifications are switched off; push only.",
                    tournament.getName(), tournament.getId());
        }
        return dispatched;
    }

    private int dispatch(Tournament tournament, List<Player> pending, String channel,
                         String reminderType, ZoneId zone) {

        List<Player> eligible = filterEligible(tournament, pending, channel, reminderType, zone);
        if (eligible.isEmpty()) {
            log.info("Tournament '{}' ({}): no players eligible for a {} {} today.",
                    tournament.getName(), tournament.getId(), channel, reminderType);
            return 0;
        }

        List<Player> delivered;
        if (CHANNEL_EMAIL.equals(channel)) {
            // Returns only the recipients the mail server accepted, so a bounce is never logged as sent
            // and the player stays eligible tomorrow.
            delivered = rsvpEmailService.sendRsvpEmails(tournament, eligible, TYPE_INVITE.equals(reminderType));
        } else {
            notificationService.sendToPlayers(eligible, buildPushTitle(reminderType),
                    buildPushBody(tournament), Map.of(
                            "type", TYPE_INVITE.equals(reminderType) ? "RSVP_INVITATION" : "RSVP_REMINDER",
                            "tournamentId", String.valueOf(tournament.getId())));
            // FCM multicast reports per-token results, not per-player, so treat the batch as sent.
            delivered = eligible;
        }

        recordLogs(tournament, delivered, channel, reminderType);

        log.info("Tournament '{}' ({}): {} {} sent to {} player(s).",
                tournament.getName(), tournament.getId(), channel, reminderType, delivered.size());
        return delivered.size();
    }

    private List<Player> filterEligible(Tournament tournament, List<Player> pending, String channel,
                                        String reminderType, ZoneId zone) {

        LocalDate today = LocalDate.now(zone);
        Set<Long> contactedToday = new HashSet<>(reminderLogRepository.findPlayerIdsContactedBetween(
                tournament.getId(), channel,
                startOfDayInUtc(today, zone), startOfDayInUtc(today.plusDays(1), zone)));

        List<Long> playerIds = pending.stream().map(Player::getId).toList();
        Map<Long, Long> reminderCounts = new HashMap<>();
        // Invitations are excluded from this count, so the announcement never burns a reminder slot.
        reminderLogRepository.countByTournamentPlayersChannelAndType(
                        tournament.getId(), playerIds, channel, TYPE_REMINDER)
                .forEach(row -> reminderCounts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue()));

        boolean invitation = TYPE_INVITE.equals(reminderType);
        List<Player> eligible = new ArrayList<>();
        for (Player player : pending) {
            if (contactedToday.contains(player.getId())) {
                continue;
            }
            if (!invitation && reminderCounts.getOrDefault(player.getId(), 0L) >= maxPerPlayer) {
                continue;
            }
            eligible.add(player);
        }
        return eligible;
    }

    private void recordLogs(Tournament tournament, List<Player> delivered, String channel, String reminderType) {
        if (delivered.isEmpty()) {
            return;
        }
        LocalDateTime sentAt = utcNow();
        List<TournamentReminderLog> logs = new ArrayList<>();
        for (Player player : delivered) {
            logs.add(TournamentReminderLog.builder()
                    .tournament(tournament)
                    .player(player)
                    .channel(channel)
                    .reminderType(reminderType)
                    .sentAt(sentAt)
                    .build());
        }
        reminderLogRepository.saveAll(logs);
    }

    /**
     * Every stored timestamp is UTC: the app pins the JVM default zone to UTC and the client converts
     * tournament dates to UTC before posting them. Made explicit here so the logic survives a host
     * whose clock is not UTC.
     */
    private LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Midnight of a {@code zone} calendar day, expressed on the UTC timeline the stored timestamps use.
     * Without this the Dhaka day boundary would be compared against UTC values and drift six hours -
     * enough to mis-bucket anything sent between 18:00 and midnight UTC.
     */
    private LocalDateTime startOfDayInUtc(LocalDate day, ZoneId zone) {
        return day.atStartOfDay(zone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private String buildPushTitle(String reminderType) {
        return TYPE_INVITE.equals(reminderType) ? "⚽ New tournament scheduled" : "⚽ Are you playing?";
    }

    private String buildPushBody(Tournament tournament) {
        // Same UTC-to-club-time conversion the emails use, so both channels quote the same kickoff.
        String kickoff = tournament.getTournamentDate()
                .atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(ZoneId.of(reminderZone))
                .format(DISPLAY_FORMAT);
        return String.format("%s is on %s. Tap to confirm Yes or No.", tournament.getName(), kickoff);
    }
}
