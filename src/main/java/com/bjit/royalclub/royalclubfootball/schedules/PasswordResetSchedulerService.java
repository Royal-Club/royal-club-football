package com.bjit.royalclub.royalclubfootball.schedules;

import com.bjit.royalclub.royalclubfootball.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetSchedulerService {

    private final PasswordResetService passwordResetService;

    /**
     * Settles reset links left PENDING by a process that died between writing the row and finding
     * out whether the email went out. Until reaped, such a row holds one of the member's three
     * monthly slots for mail they may never have received.
     * <p>
     * Runs every ten minutes rather than on a daily cron because the cost of waiting is borne by a
     * member who is already locked out. It is a cheap indexed update over a table with a handful of
     * rows, and a no-op whenever nothing is stale — which is almost always.
     */
    @Scheduled(fixedDelayString = "${password-reset.reaper-interval-ms:600000}",
            initialDelayString = "${password-reset.reaper-initial-delay-ms:60000}")
    public void reconcileStalePasswordResetLinks() {
        int reaped = passwordResetService.reconcileStalePendingLinks();
        if (reaped > 0) {
            log.info("Password reset reaper finished; settled {} abandoned link(s).", reaped);
        }
    }
}
