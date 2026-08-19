package com.bjit.royalclub.royalclubfootball.storage.teamchat;

import com.bjit.royalclub.royalclubfootball.model.TeamLogoUploadResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Storage for files shared inside a team chat room.
 *
 * <p>Deliberately a separate namespace from the resource library rather than a reuse of it. Chat
 * files are destroyed wholesale when a tournament concludes, and that purge works from storage keys;
 * sharing a namespace would let a member attach a known resource-library key to their own message
 * and have the purge delete the club's own document along with the chat.
 *
 * <p>Keys carry the team they were issued for - {@code team-chat-{teamId}-{uuid}.ext}. The bytes go
 * straight to storage over a presigned URL and only the key comes back through the API, so without
 * that the server would have to take the caller's word for which room a file belongs to. Reading the
 * team out of the key instead makes the check stateless: no table of pending uploads to keep, and
 * nothing to expire.
 */
public interface TeamChatFileStorageProvider {

    /** Prefix every key this provider issues carries, so a foreign key can be rejected on sight. */
    String KEY_PREFIX = "team-chat-";

    /**
     * An upload slot for one file in one room.
     *
     * @param sizeBytes exact size of the file about to be uploaded. Enforced by the provider where
     *                  it can be - the S3 provider signs it into the URL - because the bytes never
     *                  pass through this application, so an unenforced size is only ever a claim.
     */
    TeamLogoUploadResponse generateUploadUrl(Long teamId, String fileName, String contentType,
                                             long sizeBytes);

    InputStream load(String key) throws IOException;

    String detectContentType(String key) throws IOException;

    void delete(String key);

    /**
     * Deletes every object this provider holds for one team, whether or not a message ever
     * referenced it.
     *
     * <p>Deleting only the keys recorded in {@code team_chat_attachment} would leave behind anything
     * a member uploaded and then thought better of sending: the bytes reach storage the moment the
     * upload finishes, but the row is only written when the message is posted. Those files have no
     * row, so nothing would ever come back for them - and "the chat is deleted when the tournament
     * ends" has to be true of them as well.
     *
     * <p>Unlike {@link #delete(String)}, which shrugs off its own failures, this either removes
     * everything or says so. The purge clears the room's state once this returns, and after that the
     * room no longer matches the sweep that would have retried it - so a silent partial failure here
     * would strand files in storage permanently, with nothing left to find them by.
     *
     * @return how many objects were removed
     * @throws com.bjit.royalclub.royalclubfootball.exception.TeamChatStorageException
     *         if the sweep could not be completed, so the caller can leave the room to be retried
     */
    int deleteAllForTeam(Long teamId);

    /** The prefix every key for one team shares. */
    static String teamPrefix(Long teamId) {
        return KEY_PREFIX + teamId + "-";
    }

    default void save(String key, InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Direct save is only supported by local storage provider");
    }

    /** A fresh key for one upload into one room. */
    static String keyFor(Long teamId, String fileName) {
        return KEY_PREFIX + teamId + "-" + UUID.randomUUID() + safeExtension(fileName);
    }

    /**
     * True when the key looks like one of ours. Rejects path separators and traversal outright: the
     * local provider resolves keys against a directory, and the R2 one against a bucket root.
     */
    static boolean isTeamChatKey(String key) {
        return key != null
                && key.startsWith(KEY_PREFIX)
                && !key.contains("/")
                && !key.contains("\\")
                && !key.contains("..");
    }

    /** True when the key was issued for this team specifically. */
    static boolean belongsToTeam(String key, Long teamId) {
        return isTeamChatKey(key) && teamId != null && key.startsWith(KEY_PREFIX + teamId + "-");
    }

    /**
     * The original extension when it is plainly safe, and {@code .bin} otherwise. The name a caller
     * supplies never reaches the filesystem intact - only this suffix does.
     */
    static String safeExtension(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return ".bin";
        }
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFileName.length() - 1) {
            return ".bin";
        }
        String extension = originalFileName.substring(dotIndex).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : ".bin";
    }
}
