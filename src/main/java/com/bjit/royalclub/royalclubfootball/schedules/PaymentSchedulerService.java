package com.bjit.royalclub.royalclubfootball.schedules;

import com.bjit.royalclub.royalclubfootball.service.notification.MonthlyDuesReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentSchedulerService {

    private final MonthlyDuesReminderService monthlyDuesReminderService;

    /**
     * Remind players who have not paid this month's dues, at 10:00 AM on the 5th, 10th and 15th
     * (Asia/Dhaka), on push and by email. The days-of-month in the cron are what bound the window —
     * the service itself has no date logic, so retuning the schedule needs no code change.
     *
     * "0 0 10 5,10,15 * ?" -> Seconds Minutes Hours DayOfMonth Month DayOfWeek
     */
    @Scheduled(cron = "${dues-reminders.cron:0 0 10 5,10,15 * ?}", zone = "${dues-reminders.zone:Asia/Dhaka}")
    public void sendMonthlyDuesReminders() {
        int reminded = monthlyDuesReminderService.sendDueReminders();
        log.info("Monthly dues reminder job finished; dispatched {} reminder(s).", reminded);
    }
}
