package com.bjit.royalclub.royalclubfootball.schedules;

import com.bjit.royalclub.royalclubfootball.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenSchedulerService {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Drops refresh tokens that are past their expiry.
     * <p>
     * Purely housekeeping: rotation writes a row on every renewal, so an active device adds one per
     * access-token lifetime forever, and nothing ever reads a row once it is expired. Deleting on
     * expiry rather than on revocation is deliberate — a revoked row is what reuse detection reads,
     * so it has to outlive its own revocation.
     */
    @Scheduled(cron = "${jwt.refresh-cleanup-cron:0 30 3 * * ?}", zone = "Asia/Dhaka")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        int purged = refreshTokenRepository.deleteExpiredBefore(LocalDateTime.now());
        if (purged > 0) {
            log.info("Refresh token cleanup finished; removed {} expired token(s).", purged);
        }
    }
}
