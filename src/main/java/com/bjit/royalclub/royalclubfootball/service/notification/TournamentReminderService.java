package com.bjit.royalclub.royalclubfootball.service.notification;

public interface TournamentReminderService {

    /**
     * Scan for tournaments within the reminder window and push a Yes/No reminder to every player who
     * has not responded yet (respecting the per-player cap). Invoked by the scheduler.
     *
     * @return total number of reminders dispatched across all due tournaments.
     */
    int sendDueReminders();

    /**
     * Manually nudge all still-pending players for a specific tournament (admin "remind now").
     *
     * @return number of reminders dispatched.
     */
    int remindForTournament(Long tournamentId);
}
