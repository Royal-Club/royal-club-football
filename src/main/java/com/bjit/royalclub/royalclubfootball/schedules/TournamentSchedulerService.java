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

    // Cron expression for 12:15 AM and 8:00 AM every day
    // "0 15 0,8 * * ?" -> Seconds Minutes Hours DayOfMonth Month DayOfWeek Year(optional)
    @Scheduled(cron = "0 15 0,8 * * ?", zone = "Asia/Dhaka")
    @Transactional
    public void deactivatePastTournaments() {
        tournamentService.deactivateAndConcludePastTournaments();
        log.info("Deactivated and conclude past tournaments based on tournamentDate.");
    }
}
