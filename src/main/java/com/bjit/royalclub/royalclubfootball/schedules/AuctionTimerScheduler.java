package com.bjit.royalclub.royalclubfootball.schedules;

import com.bjit.royalclub.royalclubfootball.entity.auction.AuctionSession;
import com.bjit.royalclub.royalclubfootball.enums.AuctionSessionStatus;
import com.bjit.royalclub.royalclubfootball.repository.auction.AuctionSessionRepository;
import com.bjit.royalclub.royalclubfootball.service.auction.AuctionSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionTimerScheduler {

    private final AuctionSessionRepository sessionRepository;
    private final AuctionSessionService sessionService;

    /**
     * Checks every second for auction timers that have expired.
     * When a timer expires: clears the timer and broadcasts TIMER_EXPIRED.
     * Admin must manually click Sell or Unsold — no auto-resolution.
     */
    @Scheduled(fixedDelay = 1000)
    public void checkExpiredAuctionTimers() {
        try {
            List<AuctionSession> activeSessions = sessionRepository
                    .findAllByStatusAndCurrentTimerEndsAtIsNotNull(AuctionSessionStatus.RUNNING);

            for (AuctionSession session : activeSessions) {
                if (session.getCurrentAuctionPlayer() != null
                        && session.getCurrentTimerEndsAt() != null
                        && LocalDateTime.now().isAfter(session.getCurrentTimerEndsAt())) {
                    Long tournamentId = session.getTournament().getId();
                    try {
                        sessionService.notifyTimerExpired(tournamentId);
                    } catch (Exception e) {
                        log.error("Failed to notify timer expiry for tournament {}: {}", tournamentId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in auction timer scheduler: {}", e.getMessage());
        }
    }
}
