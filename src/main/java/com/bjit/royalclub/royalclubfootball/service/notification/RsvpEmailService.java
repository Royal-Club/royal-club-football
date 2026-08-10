package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Tournament;

import java.util.List;

/**
 * Sends the tournament invitation and RSVP reminder emails.
 * <p>
 * Kept separate from the channel-agnostic {@link NotificationService} because every recipient gets a
 * different body: the Yes/No links are signed per player, so there is no shared message to broadcast.
 */
public interface RsvpEmailService {

    /**
     * Emails each player their own Yes/No links for the tournament.
     *
     * @param invitation true for the one-off creation announcement, false for a D-2/D-1/match-day nudge.
     * @return the players the mail server accepted. Failures are logged and omitted, never thrown, so one
     * bad address cannot abort the run - and an unsent email is never recorded as sent.
     */
    List<Player> sendRsvpEmails(Tournament tournament, List<Player> players, boolean invitation);
}
