package com.bjit.royalclub.royalclubfootball.storage.teamchat;

import com.bjit.royalclub.royalclubfootball.exception.TeamChatStorageException;
import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class TeamChatFileLocalStorageProvider implements TeamChatFileStorageProvider {

    private final Path uploadPath;
    private final String baseUrl;

    public TeamChatFileLocalStorageProvider(String uploadDir, String baseUrl) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    @Override
    public TeamLogoUploadResponse generateUploadUrl(Long teamId, String fileName,
                                                    String contentType, long sizeBytes) {
        String key = TeamChatFileStorageProvider.keyFor(teamId, fileName);
        return TeamLogoUploadResponse.builder()
                .key(key)
                // No readable URL is handed out: chat files are served only through the
                // membership-gated download route, which is addressed by attachment id rather
                // than by storage key.
                .url(null)
                .uploadUrl(baseUrl + "/files/team-chat/local/" + key)
                .expiresInSeconds(3600L)
                .build();
    }

    @Override
    public void save(String key, InputStream inputStream) throws IOException {
        validateKey(key);
        Files.createDirectories(uploadPath);
        Path destination = uploadPath.resolve(key).normalize();
        if (!destination.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file path");
        }
        Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream load(String key) throws IOException {
        validateKey(key);
        Path filePath = uploadPath.resolve(key).normalize();
        if (!filePath.startsWith(uploadPath) || !Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new IOException("File not found");
        }
        return Files.newInputStream(filePath);
    }

    @Override
    public String detectContentType(String key) throws IOException {
        validateKey(key);
        Path filePath = uploadPath.resolve(key).normalize();
        String contentType = Files.probeContentType(filePath);
        return contentType != null ? contentType : "application/octet-stream";
    }

    /**
     * Never throws. A purge that stopped at the first missing file would leave the rest of a room's
     * documents on disk with their rows already deleted, and nothing left pointing at them.
     */
    @Override
    public void delete(String key) {
        try {
            validateKey(key);
            Path filePath = uploadPath.resolve(key).normalize();
            if (filePath.startsWith(uploadPath)) {
                Files.deleteIfExists(filePath);
            }
        } catch (Exception ex) {
            log.warn("Failed to delete team chat file key={}: {}", key, ex.getMessage());
        }
    }

    @Override
    public int deleteAllForTeam(Long teamId) {
        String prefix = TeamChatFileStorageProvider.teamPrefix(teamId);
        if (!Files.isDirectory(uploadPath)) {
            // Nothing was ever written here. A genuinely empty sweep, not a failed one.
            return 0;
        }

        List<Path> targets;
        // A plain directory listing filtered by prefix. The store is flat and one tournament's files
        // number in the dozens, so there is nothing here worth indexing.
        try (Stream<Path> entries = Files.list(uploadPath)) {
            targets = entries
                    .filter(entry -> entry.getFileName().toString().startsWith(prefix))
                    .toList();
        } catch (IOException ex) {
            throw new TeamChatStorageException(
                    "Could not list team chat files for team " + teamId, ex);
        }

        int removed = 0;
        int failed = 0;
        for (Path entry : targets) {
            try {
                Files.deleteIfExists(entry);
                removed++;
            } catch (IOException ex) {
                // Kept going rather than bailing on the first one: the remaining files should still
                // be removed, and the throw below makes sure the room is retried regardless.
                failed++;
                log.warn("Failed to delete team chat file {}: {}", entry, ex.getMessage());
            }
        }

        if (failed > 0) {
            throw new TeamChatStorageException(
                    "Left " + failed + " team chat file(s) undeleted for team " + teamId);
        }
        return removed;
    }

    private static void validateKey(String key) {
        if (!TeamChatFileStorageProvider.isTeamChatKey(key)) {
            throw new IllegalArgumentException("Invalid team chat file key");
        }
    }

    private static String trimTrailingSlash(String value) {
        return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
