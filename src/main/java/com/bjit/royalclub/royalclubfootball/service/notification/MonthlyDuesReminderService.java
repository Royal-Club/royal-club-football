package com.bjit.royalclub.royalclubfootball.service.notification;

import java.time.LocalDate;

public interface MonthlyDuesReminderService {

    /**
     * Remind every active player who has not paid for the current month.
     * Driven by the scheduler between the 7th and 10th; see PaymentSchedulerService.
     *
     * @return the number of players reminded.
     */
    int sendDueReminders();

    /**
     * Remind unpaid players for a specific month. Used by the admin-triggered endpoint so the flow
     * can be tested without waiting for the scheduled window.
     *
     * @param month any date within the target month.
     * @return the number of players reminded.
     */
    int remindUnpaidForMonth(LocalDate month);
}
