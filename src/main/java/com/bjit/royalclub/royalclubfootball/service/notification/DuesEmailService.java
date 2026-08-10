package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.Player;

import java.time.LocalDate;
import java.util.List;

/**
 * Sends the monthly club dues reminder emails.
 * <p>
 * Purely informational: dues are settled offline and recorded by an admin, so there is no action a
 * member can take from the email itself beyond checking their payments page.
 */
public interface DuesEmailService {

    /**
     * @param month first day of the month being chased. Only ever the current month - an unpaid
     *              month is not mentioned again once the next one begins.
     * @return the players the mail server accepted; failures are logged and omitted.
     */
    List<Player> sendDuesEmails(List<Player> players, LocalDate month);
}
