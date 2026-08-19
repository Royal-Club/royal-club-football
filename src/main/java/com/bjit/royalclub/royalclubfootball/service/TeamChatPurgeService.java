package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.exception.TeamChatStorageException;
import com.bjit.royalclub.royalclubfootball.repository.TeamChatMessageRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Purges every open room whose tournament has concluded.
     *
     * @return how many rooms were destroyed
     */
    @Transactional
    public int purgeConcludedRooms() {
        List<Team> teams = teamRepository.findTeamsWithChatToPurge();
        if (teams.isEmpty()) {
            return 0;
        }
        // Counted rather than assumed: a room whose files could not be deleted is left untouched and
        // will reappear here on the next run, so "found" and "purged" are not the same number.
        int purged = (int) teams.stream().filter(this::purgeRoom).count();
        log.info("Purged {} of {} team chat room(s) whose tournament has concluded.",
                purged, teams.size());
        return purged;
    }

    /**
     * Purges the rooms of one tournament, whatever its stored status.
     *
     * <p>Called from the conclude path, which has already decided the tournament is over; waiting
     * for the sweep would leave a room live and writable after the players have gone home.
     */
    @Transactional
    public int purgeRoomsOfTournament(Long tournamentId) {
        List<Team> teams = teamRepository.findTeamsWithOpenChatByTournamentId(tournamentId);
        int purged = (int) teams.stream().filter(this::purgeRoom).count();
        if (!teams.isEmpty()) {
            log.info("Purged {} of {} team chat room(s) for concluded tournament {}.",
                    purged, teams.size(), tournamentId);
        }
        return purged;
    }

    /**
     * @return true when the room was fully destroyed; false when it was left for the next sweep
     */
    private boolean purgeRoom(Team team) {
        // Storage first - see the class note. Swept by key prefix rather than by the keys recorded in
        // the attachment table, because a file a member uploaded and then decided not to send has no
        // row at all: the bytes land in storage when the upload finishes, the row only when the
        // message is posted. Deleting just the recorded keys would leave those behind forever.
        int filesRemoved;
        try {
            filesRemoved = fileStorageProvider.deleteAllForTeam(team.getId());
        } catch (TeamChatStorageException ex) {
            // Everything below is skipped deliberately. Clearing chatOpenedAt is what takes this room
            // out of findTeamsWithChatToPurge(), so doing it after a failed sweep would retire the
            // only record that these files need deleting - and they would sit in storage forever.
            // Leaving the room exactly as it was means the next sweep tries the whole thing again.
            log.error("Team chat purge deferred for team {} ({}): {}",
                    team.getTeamName(), team.getId(), ex.getMessage());
            return false;
        }

        int deleted = messageRepository.deleteByTeamId(team.getId());

        // Clears the room itself, not just its contents. Without this the room would still read as
        // open and members could start a fresh conversation in a finished tournament.
        team.setChatOpenedAt(null);
        teamRepository.save(team);

        // Tells anyone still sitting in the room to close it, rather than leaving them typing into a
        // window whose next send will be refused.
        messagingTemplate.convertAndSend(
                TeamChatServiceImpl.topicFor(team.getId()),
                Map.of("type", "ROOM_CLOSED", "teamId", team.getId()));

        log.info("Team chat purged for team {} ({}): {} message(s), {} file(s).",
                team.getTeamName(), team.getId(), deleted, filesRemoved);
        return true;
    }
}
