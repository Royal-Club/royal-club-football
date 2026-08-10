package com.bjit.royalclub.royalclubfootball.service.notification;

public interface TournamentReminderService {

    /**
     * Scan for tournaments due a nudge today (D-2, D-1 or match day before kickoff) and contact every
     * player who has not answered Yes or No yet, on push and - unless the tournament has email switched
     * off - by email. Invoked by the scheduler.
     *
     * @return total number of messages dispatched across all due tournaments and channels.
     */
    int sendDueReminders();

    /**
     * Manually nudge all still-pending players for a specific tournament (admin "remind now").
     *
     * @return number of messages dispatched.
     */
    int remindForTournament(Long tournamentId);

    /**
     * One-off announcement when a tournament is created, inviting every active member to answer.
     * Does not consume any of the player's three reminders, but does occupy the current calendar day -
     * so a tournament created on D-1 sends this instead of, not in addition to, the D-1 reminder.
     *
     * @return number of messages dispatched.
     */
    int sendInvitations(Long tournamentId);
}
