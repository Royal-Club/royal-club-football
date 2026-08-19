package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.exception.TeamChatStorageException;
import com.bjit.royalclub.royalclubfootball.repository.TeamChatMessageRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

/**
 * Destroys team chat rooms once their tournament has concluded.
 *
 * <p>This is the half of the feature that makes the other half safe to use. Players were told the
 * room disappears with the tournament, so the deletion is real: message rows, attachment rows, and
 * the stored files themselves. Nothing is soft-deleted, archived, or exported first, and there is no
 * admin view that outlives the purge - a "just in case" copy would quietly turn a temporary room
 * into a permanent record of what people said.
 *
 * <p><b>Order matters.</b> Files are removed from storage first, and the room is only marked closed
 * once that has fully succeeded. Clearing the room is what takes it out of the sweep query, so doing
 * that after a partial deletion would retire the only record that those files still need removing.
 * A failed sweep therefore changes nothing at all, and the next run retries the room from the top.
 *
 * <p><b>Transactions are deliberately narrow.</b> Deleting a room's files means round trips to
 * object storage, and this class must never make them with a pooled database connection held open -
 * a handful of unreachable objects would then tie up the connection pool rather than just failing.
 * So nothing here is annotated {@code @Transactional}: the storage sweep runs with no transaction at
 * all, and only the row deletion is wrapped, one short transaction per room. That also means one
 * room's failure cannot roll back another room's successful purge.
 *
 * <p>Reached two ways, both idempotent: the daily sweep, and directly when a coordinator concludes a
 * tournament by hand so the rooms close then rather than hours later.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamChatPurgeService {

    private final TeamRepository teamRepository;
    private final TeamChatMessageRepository messageRepository;
    private final TeamChatFileStorageProvider fileStorageProvider;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate transactionTemplate;

    /** Just enough of a team to purge it and say so in the log, held outside any transaction. */
    private record RoomRef(Long teamId, String teamName) {
    }

    /**
     * Purges every open room whose tournament has concluded.
     *
     * @return how many rooms were fully destroyed
     */
    public int purgeConcludedRooms() {
        List<RoomRef> rooms = transactionTemplate.execute(status ->
                teamRepository.findTeamsWithChatToPurge().stream()
                        .map(team -> new RoomRef(team.getId(), team.getTeamName()))
                        .toList());

        if (rooms == null || rooms.isEmpty()) {
            return 0;
        }
        // Counted rather than assumed: a room whose files could not be deleted is left untouched and
        // reappears here on the next run, so "found" and "purged" are not the same number.
        int purged = (int) rooms.stream().filter(this::purgeRoom).count();
        log.info("Purged {} of {} team chat room(s) whose tournament has concluded.",
                purged, rooms.size());
        return purged;
    }

    /**
     * Purges the rooms of one tournament, whatever its stored status.
     *
     * <p>Called from the conclude path, which has already decided the tournament is over; waiting
     * for the sweep would leave a room live and writable after the players have gone home.
     */
    public int purgeRoomsOfTournament(Long tournamentId) {
        List<RoomRef> rooms = transactionTemplate.execute(status ->
                teamRepository.findTeamsWithOpenChatByTournamentId(tournamentId).stream()
                        .map(team -> new RoomRef(team.getId(), team.getTeamName()))
                        .toList());

        if (rooms == null || rooms.isEmpty()) {
            return 0;
        }
        int purged = (int) rooms.stream().filter(this::purgeRoom).count();
        log.info("Purged {} of {} team chat room(s) for concluded tournament {}.",
                purged, rooms.size(), tournamentId);
        return purged;
    }

    /**
     * @return true when the room was fully destroyed; false when it was left for the next sweep
     */
    private boolean purgeRoom(RoomRef room) {
        // Storage first, and with no transaction open - see the class note. Swept by key prefix
        // rather than by the keys recorded in the attachment table, because a file a member uploaded
        // and then decided not to send has no row at all: the bytes land in storage when the upload
        // finishes, the row only when the message is posted. Deleting just the recorded keys would
        // leave those behind forever.
        int filesRemoved;
        try {
            filesRemoved = fileStorageProvider.deleteAllForTeam(room.teamId());
        } catch (TeamChatStorageException ex) {
            // Everything below is skipped deliberately. Clearing chatOpenedAt is what takes this room
            // out of findTeamsWithChatToPurge(), so doing it after a failed sweep would retire the
            // only record that these files need deleting - and they would sit in storage forever.
            // Leaving the room exactly as it was means the next sweep tries the whole thing again.
            log.error("Team chat purge deferred for team {} ({}): {}",
                    room.teamName(), room.teamId(), ex.getMessage());
            return false;
        }

        Integer deleted = transactionTemplate.execute(status -> eraseRows(room.teamId()));

        // Broadcast after the transaction has committed, not inside it: a member told the room is
        // gone must not then find it still there because the commit failed.
        messagingTemplate.convertAndSend(
                TeamChatServiceImpl.topicFor(room.teamId()),
                Map.of("type", "ROOM_CLOSED", "teamId", room.teamId()));

        log.info("Team chat purged for team {} ({}): {} message(s), {} file(s).",
                room.teamName(), room.teamId(), deleted, filesRemoved);
        return true;
    }

    /** The whole database side of one purge, and the only part that needs a transaction. */
    private int eraseRows(Long teamId) {
        int deleted = messageRepository.deleteByTeamId(teamId);

        // Clears the room itself, not just its contents. Without this the room would still read as
        // open and members could start a fresh conversation in a finished tournament.
        teamRepository.findById(teamId).ifPresent(team -> {
            team.setChatOpenedAt(null);
            teamRepository.save(team);
        });
        return deleted;
    }
}
