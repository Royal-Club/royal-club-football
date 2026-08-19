package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.entity.TeamChatAttachment;
import com.bjit.royalclub.royalclubfootball.exception.TeamServiceException;
import com.bjit.royalclub.royalclubfootball.model.TeamChatMessageRequest;
import com.bjit.royalclub.royalclubfootball.model.TeamChatMessageResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamChatRoomResponse;
import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;
import com.bjit.royalclub.royalclubfootball.repository.TeamChatAttachmentRepository;
import com.bjit.royalclub.royalclubfootball.service.TeamChatAccessService;
import com.bjit.royalclub.royalclubfootball.service.TeamChatService;
import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileStorageProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.TEAM_CHAT_ATTACHMENT_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

/**
 * The team chat room.
 *
 * <p>Every route here is authenticated and then narrowed further by
 * {@link TeamChatAccessService} to the players in that one squad -
 * {@code @PreAuthorize} can establish that somebody is signed in, but not that they are in this
 * team, and "signed in" is not the bar for a private room.
 */
@RestController
@RequestMapping("/team-chats")
@RequiredArgsConstructor
public class TeamChatController {

    private final TeamChatService teamChatService;
    private final TeamChatAccessService accessService;
    private final TeamChatAttachmentRepository attachmentRepository;
    private final TeamChatFileStorageProvider fileStorageProvider;

    /**
     * The signed-in player's own room for a tournament.
     *
     * <p>The entry point for players, because it needs no team id: someone on a squad should not have
     * to know their team's database id to find their own chat.
     *
     * @return 204 when they are not on a team in this tournament
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tournaments/{tournamentId}/my-room")
    public ResponseEntity<Object> myRoom(@PathVariable Long tournamentId) {
        Optional<TeamChatRoomResponse> room = teamChatService.getMyRoom(tournamentId);
        return room
                .<ResponseEntity<Object>>map(response -> buildSuccessResponse(HttpStatus.OK, FETCH_OK, response))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * Every open room the signed-in player is in.
     *
     * <p>What the dock asks on load, from whatever page it happens to be on. Always 200 with a list,
     * empty included: "you have no open room" is the normal answer for most of the week and is not a
     * condition the caller should have to tell apart from an error.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-open-rooms")
    public ResponseEntity<Object> myOpenRooms() {
        List<TeamChatRoomResponse> rooms = teamChatService.getMyOpenRooms();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, rooms, rooms.size());
    }

    /** A specific room, for a member of it. Answers with a reason when the room is closed. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{teamId}")
    public ResponseEntity<Object> room(@PathVariable Long teamId) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, teamChatService.getRoom(teamId));
    }

    /**
     * A page of history, oldest-first within the page.
     *
     * @param before id of the oldest message the caller already holds, when scrolling back
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{teamId}/messages")
    public ResponseEntity<Object> messages(@PathVariable Long teamId,
                                           @RequestParam(required = false) Long before,
                                           @RequestParam(defaultValue = "50") int limit) {
        List<TeamChatMessageResponse> messages = teamChatService.getMessages(teamId, before, limit);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, messages, messages.size());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{teamId}/messages")
    public ResponseEntity<Object> post(@PathVariable Long teamId,
                                       @Valid @RequestBody TeamChatMessageRequest request) {
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK,
                teamChatService.postMessage(teamId, request));
    }

    /**
     * An upload slot for a file headed into this room.
     *
     * <p>Gated on membership even though it writes nothing: the key it returns is what binds the
     * eventual upload to this team, so handing one to an outsider would be handing them a way in.
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{teamId}/attachments/presign")
    public ResponseEntity<Object> presign(@PathVariable Long teamId,
                                          @RequestParam String fileName,
                                          @RequestParam String contentType,
                                          @RequestParam long sizeBytes) {
        TeamLogoUploadResponse response =
                teamChatService.presignAttachment(teamId, fileName, contentType, sizeBytes);
        return buildSuccessResponse(HttpStatus.OK, "Presigned URL generated", response);
    }

    /**
     * The bytes of one shared file.
     *
     * <p>Served through the application rather than from a public storage URL. A document shared in a
     * private room is part of the conversation, and it has to become just as unreachable when the
     * room is destroyed - which a link anyone could keep would not.
     *
     * <p>The attachment is loaded together with its team so the membership check runs against the
     * room the file actually belongs to, not against the id in the path.
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{teamId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> download(@PathVariable Long teamId, @PathVariable Long attachmentId) {
        TeamChatAttachment attachment = attachmentRepository.findByIdWithTeam(attachmentId)
                .orElseThrow(() -> new TeamServiceException(TEAM_CHAT_ATTACHMENT_NOT_FOUND, HttpStatus.NOT_FOUND));

        // Authorise against the file's own room, then confirm the caller asked for the right one.
        accessService.requireOpenRoom(attachment.getMessage().getTeam().getId());
        if (!attachment.getMessage().getTeam().getId().equals(teamId)) {
            throw new TeamServiceException(TEAM_CHAT_ATTACHMENT_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        try (InputStream inputStream = fileStorageProvider.load(attachment.getStorageKey())) {
            byte[] content = StreamUtils.copyToByteArray(inputStream);
            return ResponseEntity.ok()
                    // Never cached by shared caches: the whole point is that this stops being
                    // retrievable once the tournament is over.
                    .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                            .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                            .build()
                            .toString())
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .body(content);
        } catch (IOException | IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
