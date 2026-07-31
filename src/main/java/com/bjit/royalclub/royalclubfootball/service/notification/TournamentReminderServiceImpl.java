package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;
import com.bjit.royalclub.royalclubfootball.entity.TournamentReminderLog;
import com.bjit.royalclub.royalclubfootball.exception.TournamentServiceException;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentReminderLogRepository;
import com.bjit.royalclub.royalclubfootball.repository.TournamentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TOURNAMENT_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentReminderServiceImpl implements TournamentReminderService {

    private static final String CHANNEL_PUSH = "PUSH";
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM 'at' hh:mm a");

    private final TournamentRepository tournamentRepository;
    private final PlayerRepository playerRepository;
    private final TournamentReminderLogRepository reminderLogRepository;
    private final NotificationService notificationService;

    @Value("${reminders.window-hours:48}")
    private long windowHours;

    @Value("${reminders.max-per-player:3}")
    private int maxPerPlayer;

    @Override
    @Transactional
    public int sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusHours(windowHours);
        List<Tournament> dueTournaments = tournamentRepository.findUpcomingWithinWindow(now, windowEnd);

        if (dueTournaments.isEmpty()) {
            log.info("No upcoming tournaments within the next {} hours; no reminders to send.", windowHours);
            return 0;
        }

        int total = 0;
        for (Tournament tournament : dueTournaments) {
            total += remindPendingPlayers(tournament);
        }
        log.info("Reminder run complete: {} reminder(s) dispatched across {} tournament(s).",
                total, dueTournaments.size());
        return total;
    }

    @Override
    @Transactional
    public int remindForTournament(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentServiceException(TOURNAMENT_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        if (tournament.getTournamentDate().isBefore(LocalDateTime.now())) {
            log.info("Tournament {} has already started; skipping manual reminder.", tournamentId);
            return 0;
        }
        return remindPendingPlayers(tournament);
    }

    /**
     * Push a Yes/No reminder to every still-pending player for the tournament who is under the reminder cap,
     * then record one reminder-log row per player so we can stop after {@code maxPerPlayer} nudges.
     */
    private int remindPendingPlayers(Tournament tournament) {
        List<Player> pendingPlayers = playerRepository.findActivePlayersWithoutParticipation(tournament.getId());

        if (pendingPlayers.isEmpty()) {
            log.info("Tournament '{}' ({}): no pending players eligible for a reminder.",
                    tournament.getName(), tournament.getId());
            return 0;
        }

        // Batch fetch reminder counts for all pending players
        List<Long> playerIds = pendingPlayers.stream().map(Player::getId).toList();
        Map<Long, Long> reminderCounts = new HashMap<>();
        reminderLogRepository.countByTournamentIdAndPlayerIds(tournament.getId(), playerIds)
                .forEach(row -> reminderCounts.put((Long) row[0], (Long) row[1]));

        List<Player> eligible = new ArrayList<>();
        for (Player player : pendingPlayers) {
            long alreadySent = reminderCounts.getOrDefault(player.getId(), 0L);
            if (alreadySent < maxPerPlayer) {
                eligible.add(player);
            }
        }

        if (eligible.isEmpty()) {
            log.info("Tournament '{}' ({}): no pending players eligible for a reminder.",
                    tournament.getName(), tournament.getId());
            return 0;
        }

        String title = "⚽ Are you playing?";
        String body = String.format("%s is on %s. Tap to confirm Yes or No.",
                tournament.getName(), tournament.getTournamentDate().format(DISPLAY_FORMAT));
        Map<String, String> data = Map.of(
                "type", "RSVP_REMINDER",
                "tournamentId", String.valueOf(tournament.getId()));

        notificationService.sendToPlayers(eligible, title, body, data);

        LocalDateTime sentAt = LocalDateTime.now();
        eligible.forEach(player -> reminderLogRepository.save(TournamentReminderLog.builder()
                .tournament(tournament)
                .player(player)
                .channel(CHANNEL_PUSH)
                .sentAt(sentAt)
                .build()));

        log.info("Tournament '{}' ({}): reminded {} pending player(s).",
                tournament.getName(), tournament.getId(), eligible.size());
        return eligible.size();
    }
}
