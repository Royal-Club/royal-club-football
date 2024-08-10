package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.MatchSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {
    List<MatchSchedule> findByDateTimeAfter(LocalDateTime dateTime);
}
