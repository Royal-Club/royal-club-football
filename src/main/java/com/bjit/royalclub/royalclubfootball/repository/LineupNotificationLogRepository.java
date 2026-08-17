package com.bjit.royalclub.royalclubfootball.repository;

import com.bjit.royalclub.royalclubfootball.entity.LineupNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface LineupNotificationLogRepository extends JpaRepository<LineupNotificationLog, Long> {

    /**
     * Ids of players already told about this line-up.
     * <p>
     * Publishing subtracts this from the placed squad, which is what makes the button idempotent:
     * pressing it again finds nobody left to tell.
     */
    @Query("SELECT l.player.id FROM LineupNotificationLog l WHERE l.formation.id = :formationId")
    Set<Long> findNotifiedPlayerIds(@Param("formationId") Long formationId);
}
