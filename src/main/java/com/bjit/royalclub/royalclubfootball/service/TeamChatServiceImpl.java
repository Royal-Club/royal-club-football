package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.Team;
import com.bjit.royalclub.royalclubfootball.entity.TeamChatAttachment;
import com.bjit.royalclub.royalclubfootball.entity.TeamChatMessage;
import com.bjit.royalclub.royalclubfootball.entity.TeamPlayer;
import com.bjit.royalclub.royalclubfootball.exception.TeamServiceException;
import com.bjit.royalclub.royalclubfootball.model.TeamChatAttachmentRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamChatAttachmentResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamChatMessageRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamChatMessageResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamChatRoomResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamPlayerResponse;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamChatMessageRepository;
import com.bjit.royalclub.royalclubfootball.repository.TeamRepository;
import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileStorageProvider;
import com.bjit.royalclub.royalclubfootball.util.CurrentUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_EMPTY_MESSAGE;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_FILE_TOO_LARGE;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_ROOM_STORAGE_FULL;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_INVALID_ATTACHMENT;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_NOT_A_MEMBER;

/**
 * The private room each team gets between its line-up being published and its tournament finishing.
 *
 * <p>Two things are worth knowing before changing anything here.
 *
 * <p>First, <b>the room is the team</b>. There is no room entity, no membership table and no
 * invitation. A {@link Team} belongs to exactly one tournament, so a new tournament produces new
 * teams and therefore new, empty rooms, with no code needed to roll anything over and no chance of
 * last month's conversation following the squad into the next fixture.
 *
 * <p>Second, <b>nothing here is built to last</b>. Messages and files are hard-deleted when the
 * tournament concludes - see {@link TeamChatPurgeService}. So there is no soft delete, no archive
 * flag and no export: a member writing here should be able to trust that when the tournament is
 * over, it is gone.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamChatServiceImpl implements TeamChatService {

    /** Newest-first page size when the caller does not ask for one. */
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    /** One file. Images are shrunk in the browser before upload, so this bites mainly on documents. */
    public static final long MAX_FILE_BYTES = 3L * 1024 * 1024;

    /**
     * Everything one room may hold for the whole tournament, shared across its members.
     *
     * <p>A per-room budget rather than a per-player one on purpose: a squad is a handful of people
     * who can see each other's files and sort it out between themselves, and per-player accounting
     * would mean tracking who uploaded what in order to refuse them later.
     */
    public static final long MAX_ROOM_BYTES = 10L * 1024 * 1024;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain");

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamChatMessageRepository messageRepository;
    private final TeamChatAccessService accessService;
    private final TeamChatFileStorageProvider fileStorageProvider;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void openRoomOnLineupPublished(Team team) {
        if (team.getChatOpenedAt() != null) {
            // Republishing after a swap must not wipe the conversation the squad has already had.
            return;
        }
        team.setChatOpenedAt(LocalDateTime.now());
        teamRepository.save(team);
        log.info("Team chat opened for team {} ({}) on line-up publication.",
                team.getTeamName(), team.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public TeamChatRoomResponse getRoom(Long teamId) {
        return describe(accessService.requireMembership(teamId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TeamChatRoomResponse> getMyRoom(Long tournamentId) {
        Optional<Long> playerId = CurrentUserUtil.currentPlayerId();
        if (playerId.isEmpty()) {
            return Optional.empty();
        }
        // One narrow query for "which team am I on", then the full graph through the access service
        // so the response is built the same way as every other call. Scanning every team in the
        // tournament instead would fire a query per squad member before throwing the result away.
        return teamRepository.findTeamIdOfPlayerInTournament(tournamentId, playerId.get())
                .map(teamId -> describe(accessService.requireMembership(teamId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamChatMessageResponse> getMessages(Long teamId, Long beforeId, int limit) {
        accessService.requireOpenRoom(teamId);

        int size = limit <= 0 ? DEFAULT_PAGE_SIZE : Math.min(limit, MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(0, size);
        List<TeamChatMessage> page = beforeId == null
                ? messageRepository.findLatestPage(teamId, pageRequest)
                : messageRepository.findPageBefore(teamId, beforeId, pageRequest);
        if (page.isEmpty()) {
            return List.of();
        }

        // Second query, same transaction: fills in the attachments of the messages just loaded.
        // Skipping it would not break anything visibly - it would just issue one extra query per
        // message as the mapping below touches each collection.
        messageRepository.fetchAttachments(page.stream().map(TeamChatMessage::getId).toList());

        // Fetched newest-first so the page boundary is the newest messages; handed back oldest-first
        // because that is the order a conversation is read in.
        return page.stream()
                .sorted(Comparator.comparing(TeamChatMessage::getId))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TeamChatMessageResponse postMessage(Long teamId, TeamChatMessageRequest request) {
        Team team = accessService.requireOpenRoom(teamId);
        Player sender = currentPlayer();

        String body = request.getBody() == null ? null : request.getBody().trim();
        List<TeamChatAttachmentRequest> attachments =
                request.getAttachments() == null ? List.of() : request.getAttachments();

        if ((body == null || body.isEmpty()) && attachments.isEmpty()) {
            throw new TeamServiceException(TEAM_CHAT_EMPTY_MESSAGE, HttpStatus.BAD_REQUEST);
        }

        // The authoritative quota check. Presign refuses early so a member is not made to wait for an
        // upload that was never going to be accepted, but that check races two people uploading at
        // once - only this one runs inside the transaction that actually writes the rows.
        requireRoomCapacity(teamId, attachments.stream()
                .mapToLong(attachment -> attachment.getSizeBytes() == null ? 0 : attachment.getSizeBytes())
                .sum());

        TeamChatMessage message = TeamChatMessage.builder()
                .team(team)
                .sender(sender)
                .body(body == null || body.isEmpty() ? null : body)
                .attachments(new ArrayList<>())
                .build();

        for (TeamChatAttachmentRequest attachment : attachments) {
            message.getAttachments().add(toAttachment(message, attachment, teamId));
        }

        TeamChatMessage saved = messageRepository.save(message);
        TeamChatMessageResponse response = toResponse(saved);

        // Broadcast after the transaction commits, not merely after the save. save() only stages the
        // insert - the commit happens when this method returns, and if it fails every subscriber
        // would already be showing a message that is in nobody's history and never will be. The
        // socket is a convenience; the REST history is the record, and the two must not disagree.
        broadcastAfterCommit(teamId, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TeamLogoUploadResponse presignAttachment(Long teamId, String fileName,
                                                    String contentType, long sizeBytes) {
        accessService.requireOpenRoom(teamId);
        validateUploadMeta(fileName, contentType);

        if (sizeBytes <= 0 || sizeBytes > MAX_FILE_BYTES) {
            throw new TeamServiceException(TEAM_CHAT_FILE_TOO_LARGE, HttpStatus.BAD_REQUEST);
        }
        // Checked before the URL is handed out, so a member on a phone connection is not made to
        // push 3MB up the wire only to be told the room was already full. Re-checked on post.
        requireRoomCapacity(teamId, sizeBytes);

        return fileStorageProvider.generateUploadUrl(teamId, fileName, contentType, sizeBytes);
    }

    /**
     * Refuses when this room cannot take {@code incomingBytes} more.
     *
     * @throws TeamServiceException 409, naming the shared budget rather than blaming the file
     */
    private void requireRoomCapacity(Long teamId, long incomingBytes) {
        if (incomingBytes <= 0) {
            return;
        }
        long used = messageRepository.sumAttachmentBytesByTeamId(teamId);
        if (used + incomingBytes > MAX_ROOM_BYTES) {
            throw new TeamServiceException(TEAM_CHAT_ROOM_STORAGE_FULL, HttpStatus.CONFLICT);
        }
    }

    /** The STOMP destination a room's members subscribe to. */
    public static String topicFor(Long teamId) {
        return "/topic/team-chat/" + teamId;
    }

    /**
     * Pushes the message to the room once the surrounding transaction has committed.
     *
     * <p>Falls back to sending immediately when there is no transaction to wait on, so this stays
     * correct if it is ever called outside one rather than silently dropping the broadcast.
     */
    private void broadcastAfterCommit(Long teamId, TeamChatMessageResponse response) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            messagingTemplate.convertAndSend(topicFor(teamId), response);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend(topicFor(teamId), response);
            }
        });
    }

    private TeamChatAttachment toAttachment(TeamChatMessage message,
                                            TeamChatAttachmentRequest request,
                                            Long teamId) {
        // The bytes never passed through this application, so the key is the only evidence of where
        // the file was meant to go. Keys carry their team, which is what makes this checkable at all:
        // a member cannot attach a file presigned for the opposing side's room.
        if (!TeamChatFileStorageProvider.belongsToTeam(request.getKey(), teamId)) {
            throw new TeamServiceException(TEAM_CHAT_INVALID_ATTACHMENT, HttpStatus.BAD_REQUEST);
        }
        validateUploadMeta(request.getFileName(), request.getContentType());

        return TeamChatAttachment.builder()
                .message(message)
                .storageKey(request.getKey())
                .fileName(request.getFileName())
                .contentType(request.getContentType())
                .sizeBytes(request.getSizeBytes())
                .build();
    }

    private void validateUploadMeta(String fileName, String contentType) {
        if (fileName == null || fileName.isBlank()) {
            throw new TeamServiceException("A file name is required", HttpStatus.BAD_REQUEST);
        }
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new TeamServiceException(
                    "Only images, PDFs, Office documents and plain text can be shared",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private Player currentPlayer() {
        Long playerId = CurrentUserUtil.currentPlayerId()
                .orElseThrow(() -> new TeamServiceException(TEAM_CHAT_NOT_A_MEMBER, HttpStatus.FORBIDDEN));
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new TeamServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private TeamChatRoomResponse describe(Team team) {
        Optional<String> closedReason = accessService.closedReason(team);

        return TeamChatRoomResponse.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .tournamentId(team.getTournament() != null ? team.getTournament().getId() : null)
                .tournamentName(team.getTournament() != null ? team.getTournament().getName() : null)
                .open(closedReason.isEmpty())
                .closedReason(closedReason.orElse(null))
                .openedAt(team.getChatOpenedAt())
                .members(members(team))
                // Counted rather than derived from a loaded list: a room a member has scrolled
                // through can hold hundreds of rows, and the header only needs the number.
                .messageCount(closedReason.isEmpty() ? messageRepository.countByTeamId(team.getId()) : 0)
                .storageUsedBytes(closedReason.isEmpty()
                        ? messageRepository.sumAttachmentBytesByTeamId(team.getId()) : 0)
                .storageLimitBytes(MAX_ROOM_BYTES)
                .maxFileBytes(MAX_FILE_BYTES)
                .build();
    }

    private List<TeamPlayerResponse> members(Team team) {
        if (team.getTeamPlayers() == null) {
            return List.of();
        }
        return team.getTeamPlayers().stream()
                .filter(teamPlayer -> teamPlayer.getPlayer() != null)
                .map(this::toMember)
                .toList();
    }

    private TeamPlayerResponse toMember(TeamPlayer teamPlayer) {
        Player player = teamPlayer.getPlayer();
        return TeamPlayerResponse.builder()
                .id(teamPlayer.getId())
                .teamId(teamPlayer.getTeam() != null ? teamPlayer.getTeam().getId() : null)
                .playerId(player.getId())
                .playerName(player.getName())
                .playingPosition(teamPlayer.getPlayingPosition())
                .teamPlayerRole(teamPlayer.getTeamPlayerRole() != null
                        ? teamPlayer.getTeamPlayerRole().name() : null)
                .isCaptain(teamPlayer.getIsCaptain())
                .jerseyNumber(teamPlayer.getJerseyNumber())
                .photoKey(player.getPhotoKey())
                .photoUrl(photoUrl(player.getPhotoKey()))
                .build();
    }

    private TeamChatMessageResponse toResponse(TeamChatMessage message) {
        Player sender = message.getSender();
        List<TeamChatAttachmentResponse> attachments =
                message.getAttachments() == null ? List.of() : message.getAttachments().stream()
                        .filter(Objects::nonNull)
                        .map(attachment -> TeamChatAttachmentResponse.builder()
                                .id(attachment.getId())
                                .fileName(attachment.getFileName())
                                .contentType(attachment.getContentType())
                                .sizeBytes(attachment.getSizeBytes())
                                .downloadUrl("/team-chats/" + message.getTeam().getId()
                                        + "/attachments/" + attachment.getId())
                                .build())
                        .toList();

        return TeamChatMessageResponse.builder()
                .id(message.getId())
                .teamId(message.getTeam().getId())
                .senderId(sender != null ? sender.getId() : null)
                .senderName(sender != null ? sender.getName() : null)
                .senderPhotoUrl(sender != null ? photoUrl(sender.getPhotoKey()) : null)
                .body(message.getBody())
                .sentAt(message.getCreatedDate())
                .attachments(attachments)
                .build();
    }

    private String photoUrl(String photoKey) {
        return photoKey == null || photoKey.isBlank() ? null : "/files/player-photos/" + photoKey;
    }
}
