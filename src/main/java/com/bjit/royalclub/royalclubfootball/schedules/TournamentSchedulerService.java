package com.bjit.royalclub.royalclubfootball.schedules;

import com.bjit.royalclub.royalclubfootball.service.TeamChatPurgeService;
import com.bjit.royalclub.royalclubfootball.service.TournamentService;
import com.bjit.royalclub.royalclubfootball.service.notification.TournamentReminderService;
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
    private final TeamChatPurgeService teamChatPurgeService;

    // Cron expression for 12:15 AM, 8:00 AM, and 11:00 AM every day
    // "0 15 0,8,11 * * ?" -> Seconds Minutes Hours DayOfMonth Month DayOfWeek Year(optional)
    // Deliberately not @Transactional. TournamentService.updateTournamentStatuses() carries its own
    // (declared on the interface), and the chat purge underneath makes remote calls to object
    // storage - which must not happen with a pooled database connection held open. See
    // spring.datasource.hikari.leak-detection-threshold in application.yml.
    @Scheduled(cron = "0 0 0,8,11 * * ?", zone = "Asia/Dhaka")
    public void updateTournamentStatuses() {
        tournamentService.updateTournamentStatuses();
        log.info("Updated tournament statuses based on match status and tournament date.");

        // Immediately after, in the same run, because this job is what concludes most tournaments -
        // a tournament nobody concluded by hand becomes CONCLUDED on the line above, and its team
        // chats have to go with it. Sweeping here rather than on its own schedule also means the
        // rooms can never outlive the status change by a whole cron interval.
        //
        // It re-scans for concluded tournaments rather than trusting what was just updated, so a
        // room left behind by an earlier failure is picked up on the next run instead of forever.
        int purgedRooms = teamChatPurgeService.purgeConcludedRooms();
        if (purgedRooms > 0) {
            log.info("Purged {} team chat room(s) belonging to concluded tournaments.", purgedRooms);
        }
    }

    // Nudge players who have not answered Yes/No, once a day at 9:00 AM (Asia/Dhaka).
    // The service decides who is due: D-2, D-1 and match day, provided kickoff is still ahead.
    @Scheduled(cron = "${reminders.cron:0 0 9 * * ?}", zone = "${reminders.zone:Asia/Dhaka}")
    public void sendTournamentRsvpReminders() {
        int reminded = tournamentReminderService.sendDueReminders();
        log.info("RSVP reminder job finished; dispatched {} reminder(s).", reminded);
    }
}
