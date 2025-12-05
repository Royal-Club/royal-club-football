package com.bjit.royalclub.royalclubfootball.schedules;

import com.bjit.royalclub.royalclubfootball.service.TournamentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentSchedulerService {

    private final TournamentService tournamentService;

    // Cron expression for 12:15 AM, 8:00 AM, and 11:00 AM every day
    // "0 15 0,8,11 * * ?" -> Seconds Minutes Hours DayOfMonth Month DayOfWeek Year(optional)
    @Scheduled(cron = "0 0 0,8,11 * * ?", zone = "Asia/Dhaka")
    @Transactional
    public void updateTournamentStatuses() {
        tournamentService.updateTournamentStatuses();
        log.info("Updated tournament statuses based on match status and tournament date.");
    }
}
