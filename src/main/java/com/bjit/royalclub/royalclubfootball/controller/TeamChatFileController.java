package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileLocalStorageProvider;
import com.bjit.royalclub.royalclubfootball.storage.teamchat.TeamChatFileStorageProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

/**
 * Receives chat file uploads when the app is configured for local storage.
 *
 * <p>Exists only because the local provider has no equivalent of an S3 presigned PUT; against R2 the
 * browser uploads straight to the bucket and nothing here is involved.
 *
 * <p>There is deliberately no read route to match it. Chat files are served solely by
 * {@link TeamChatController}, which checks squad membership first - a second, ungated way to fetch
 * the same bytes would make that check decorative.
 */
@RestController
@RequestMapping("/files/team-chat")
@RequiredArgsConstructor
public class TeamChatFileController {

    /** Matches the per-file cap enforced by the chat service; see TeamChatServiceImpl. */
    private static final long MAX_SIZE_BYTES = 3L * 1024 * 1024;

    private final TeamChatFileStorageProvider fileStorageProvider;

    /**
     * Stores the bytes for a key that was presigned earlier.
     *
     * <p>Authentication is required even though the key is unguessable, so an open upload endpoint
     * is not left sitting on the host. The key was issued to a member of one room and encodes that
     * team, so the worst a signed-in stranger could do is guess a UUID.
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/local/{key}")
    public ResponseEntity<Object> localUpload(@PathVariable String key,
                                              HttpServletRequest request) throws IOException {
        if (!(fileStorageProvider instanceof TeamChatFileLocalStorageProvider)) {
            return ResponseEntity.notFound().build();
        }
        if (!TeamChatFileStorageProvider.isTeamChatKey(key)) {
            throw new IllegalArgumentException("Invalid team chat file key");
        }
        if (request.getContentLengthLong() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the 3MB limit");
        }
        try (InputStream inputStream = request.getInputStream()) {
            fileStorageProvider.save(key, inputStream);
        }
        return buildSuccessResponse(HttpStatus.OK, "Upload successful", Map.of("key", key));
    }
}
