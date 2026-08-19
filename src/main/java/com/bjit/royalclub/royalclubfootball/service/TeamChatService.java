package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.model.TeamChatMessageRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamChatMessageResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamChatRoomResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;

import java.util.List;
import java.util.Optional;

public interface TeamChatService {

    /**
     * Opens a team's room. Called when the line-up is published, and only then - that is the moment
     * the squad becomes a real, settled group of people rather than a draft.
     *
     * <p>Idempotent: publishing again, or swapping a player in and republishing, leaves the original
     * opening time and the existing conversation alone.
     */
    void openRoomOnLineupPublished(Team team);

    /** The room's state for a member, including why it is unusable when it is. */
    TeamChatRoomResponse getRoom(Long teamId);

    /**
     * The signed-in player's own room for a tournament, resolved from their squad place rather than
     * from a team id they supply.
     *
     * @return empty when they are not on any team in that tournament
     */
    Optional<TeamChatRoomResponse> getMyRoom(Long tournamentId);

    /**
     * A page of history, newest first.
     *
     * @param beforeId only messages older than this one, for scrolling back; null for the latest page
     */
    List<TeamChatMessageResponse> getMessages(Long teamId, Long beforeId, int limit);

    /** Posts a message, and pushes it to everyone currently in the room. */
    TeamChatMessageResponse postMessage(Long teamId, TeamChatMessageRequest request);

    /**
     * An upload slot for a file destined for this room.
     *
     * @param sizeBytes exact size of the file. Required, and not merely so an over-budget upload can
     *                  be refused before it happens: the size is signed into the upload URL, so a
     *                  file that does not match the size budgeted for is rejected by storage itself.
     */
    TeamLogoUploadResponse presignAttachment(Long teamId, String fileName, String contentType,
                                             long sizeBytes);
}
