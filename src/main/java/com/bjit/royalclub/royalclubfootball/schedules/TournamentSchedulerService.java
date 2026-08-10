package com.bjit.royalclub.royalclubfootball.schedules;

import com.bjit.royalclub.royalclubfootball.service.TournamentService;
import com.bjit.royalclub.royalclubfootball.service.notification.TournamentReminderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentSchedulerService {

    private final TournamentService tournamentService;
    private final TournamentReminderService tournamentReminderService;

    // Cron expression for 12:15 AM, 8:00 AM, and 11:00 AM every day
    // "0 15 0,8,11 * * ?" -> Seconds Minutes Hours DayOfMonth Month DayOfWeek Year(optional)
    @Scheduled(cron = "0 0 0,8,11 * * ?", zone = "Asia/Dhaka")
    @Transactional
    public void updateTournamentStatuses() {
        tournamentService.updateTournamentStatuses();
        log.info("Updated tournament statuses based on match status and tournament date.");
    }

    // Nudge players who have not answered Yes/No, once a day at 9:00 AM (Asia/Dhaka).
    // The service decides who is due: D-2, D-1 and match day, provided kickoff is still ahead.
    @Scheduled(cron = "${reminders.cron:0 0 9 * * ?}", zone = "${reminders.zone:Asia/Dhaka}")
    public void sendTournamentRsvpReminders() {
        int reminded = tournamentReminderService.sendDueReminders();
        log.info("RSVP reminder job finished; dispatched {} reminder(s).", reminded);
    }
}
