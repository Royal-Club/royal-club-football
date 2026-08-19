package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A file in a room, as the client sees it.
 *
 * <p>The storage key is deliberately absent: the file is reachable only through {@code downloadUrl},
 * which runs the same membership check as the room itself. Handing out the key would put the object
 * one public URL away from anyone.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamChatAttachmentResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private Long sizeBytes;

    /** Member-gated route for the bytes, not a direct storage link. */
    private String downloadUrl;
}
