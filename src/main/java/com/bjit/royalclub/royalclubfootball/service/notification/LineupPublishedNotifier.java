package com.bjit.royalclub.royalclubfootball.service.notification;

import com.bjit.royalclub.royalclubfootball.entity.LineupNotificationLog;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamFormation;
import com.bjit.royalclub.royalclubfootball.entity.TeamFormationSlot;
import com.bjit.royalclub.royalclubfootball.enums.PositionGroup;
import com.bjit.royalclub.royalclubfootball.model.LineupPublishResponse;
import com.bjit.royalclub.royalclubfootball.repository.LineupNotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Announces a published line-up to the players in it.
 *
 * <p>Saving a formation never notifies anyone — a captain arranging a team saves repeatedly, and a
 * push per save would make the feature something players want turned off. Publishing is the
 * separate, deliberate act of telling the squad.
 *
 * <p>Who gets told is decided per player, not per line-up. Publishing notifies the placed players
 * with no entry in {@link LineupNotificationLog} and writes one for each. That makes the button
 * idempotent — a second press finds nobody left — and incremental: swap a player in, publish again,
 * and only the replacement hears. A player removed from the line-up keeps their row, so they are
 * never told twice if they come back.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineupPublishedNotifier {

    private final NotificationService notificationService;
    private final LineupNotificationLogRepository notificationLogRepository;

    /** State of the line-up without sending anything — what the board renders. */
    @Transactional(readOnly = true)
    public LineupPublishResponse status(TeamFormation formation) {
        Map<Player, PositionGroup> placed = placedPlayers(formation);
        Set<Long> alreadyNotified = notifiedIds(formation);
        long pending = placed.keySet().stream()
                .filter(player -> !alreadyNotified.contains(player.getId()))
                .count();

        return LineupPublishResponse.builder()
                .published(!alreadyNotified.isEmpty())
                .placedPlayers(placed.size())
                .notifiedPlayers(alreadyNotified.size())
                .pendingPlayers((int) pending)
                .notifiedNow(0)
                .build();
    }

    /**
     * Tells every placed player who has not been told yet.
     *
     * @return what the board should now show, including how many were reached by this call.
     */
    @Transactional
    public LineupPublishResponse publish(TeamFormation formation, Team team) {
        Map<Player, PositionGroup> placed = placedPlayers(formation);
        Set<Long> alreadyNotified = notifiedIds(formation);

        List<Map.Entry<Player, PositionGroup>> toNotify = placed.entrySet().stream()
                .filter(entry -> !alreadyNotified.contains(entry.getKey().getId()))
                .toList();

        int notifiedNow = 0;
        for (Map.Entry<Player, PositionGroup> entry : toNotify) {
            // One send per player so the body can name their own position. A squad is single
            // figures, so this costs a handful of calls and reads as though it was written for
            // them rather than shouted at everyone.
            notificationService.sendToPlayers(
                    List.of(entry.getKey()),
                    "⚽ Line-up is out",
                    bodyFor(entry.getValue(), team),
                    payload(team));

            // Logged whether or not the device could be reached. The ledger records that the player
            // was announced to, not that a handset lit up - otherwise someone with no app would be
            // re-notified on every future publish, which is the spam this design exists to avoid.
            notificationLogRepository.save(LineupNotificationLog.builder()
                    .formation(formation)
                    .player(entry.getKey())
                    .notifiedAt(LocalDateTime.now())
                    .build());
            notifiedNow++;
        }

        log.info("Line-up published for team {} ({}): {} newly notified, {} already knew.",
                team.getTeamName(), team.getId(), notifiedNow, alreadyNotified.size());

        return LineupPublishResponse.builder()
                .published(true)
                .placedPlayers(placed.size())
                .notifiedPlayers(alreadyNotified.size() + notifiedNow)
                .pendingPlayers(0)
                .notifiedNow(notifiedNow)
                .build();
    }

    private Set<Long> notifiedIds(TeamFormation formation) {
        return formation.getId() == null
                ? Set.of()
                : notificationLogRepository.findNotifiedPlayerIds(formation.getId());
    }

    /** Uses the position group's own description, so the app and the push say the same words. */
    private String bodyFor(PositionGroup group, Team team) {
        return String.format("You're starting at %s for %s. Tap to see it.",
                group.getDescription(), team.getTeamName());
    }

    private Map<String, String> payload(Team team) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", "LINEUP_PUBLISHED");
        data.put("teamId", String.valueOf(team.getId()));
        if (team.getTournament() != null) {
            data.put("tournamentId", String.valueOf(team.getTournament().getId()));
        }
        return data;
    }

    /**
     * Players holding a slot, and the position group they were put in.
     * <p>
     * Only placed players are considered: telling someone the line-up is out when they are not in
     * it is a different message, and not one to send by accident.
     */
    private Map<Player, PositionGroup> placedPlayers(TeamFormation formation) {
        Map<Player, PositionGroup> placed = new LinkedHashMap<>();
        for (TeamFormationSlot slot : formation.getSlots()) {
            if (slot.getTeamPlayer() == null || slot.getTeamPlayer().getPlayer() == null) {
                continue;
            }
            placed.putIfAbsent(slot.getTeamPlayer().getPlayer(),
                    Objects.requireNonNullElse(slot.getPositionGroup(), PositionGroup.MID));
        }
        return placed;
    }
}
