package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One already-uploaded file being attached to a message.
 *
 * <p>The bytes travel straight to storage over a presigned URL; only this descriptor comes back
 * through the API. The key must be one this room handed out - the service checks that rather than
 * trusting the caller, or a member could attach any object in the bucket to their own message.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamChatAttachmentRequest {

    @NotBlank(message = "Attachment key is required")
    private String key;

    @NotBlank(message = "Attachment file name is required")
    private String fileName;

    @NotBlank(message = "Attachment content type is required")
    private String contentType;

    @NotNull(message = "Attachment size is required")
    @Positive(message = "Attachment size must be greater than zero")
    @Max(value = 3_145_728L, message = "Files must be 3MB or smaller")
    private Long sizeBytes;
}
