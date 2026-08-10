package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.event.TournamentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Fires the creation invitation once the tournament is safely committed.
 * <p>
 * After-commit and async on purpose: sending to every active member over SMTP takes seconds, and the
 * admin's "create tournament" request should not wait for it - nor fail if the mail server does.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentInvitationListener {

    private final TournamentReminderService tournamentReminderService;

    @Async
    @TransactionalEventListener
    public void onTournamentCreated(TournamentCreatedEvent event) {
        try {
            int dispatched = tournamentReminderService.sendInvitations(event.tournamentId());
            log.info("Tournament {} created: {} invitation(s) dispatched.", event.tournamentId(), dispatched);
        } catch (Exception e) {
            log.error("Failed to dispatch invitations for tournament {}.", event.tournamentId(), e);
        }
    }
}
