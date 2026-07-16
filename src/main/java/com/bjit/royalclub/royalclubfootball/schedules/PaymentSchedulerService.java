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
     * Remind players who have not paid this month's dues, at 10:00 AM on the 7th through the 10th
     * (Asia/Dhaka). The day-of-month range in the cron is what bounds the window — the service itself
     * has no date logic, so retuning the schedule needs no code change.
     *
     * "0 0 10 7-10 * ?" -> Seconds Minutes Hours DayOfMonth Month DayOfWeek
     */
    @Scheduled(cron = "${dues-reminders.cron:0 0 10 7-10 * ?}", zone = "Asia/Dhaka")
    public void sendMonthlyDuesReminders() {
        int reminded = monthlyDuesReminderService.sendDueReminders();
        log.info("Monthly dues reminder job finished; dispatched {} reminder(s).", reminded);
    }
}
