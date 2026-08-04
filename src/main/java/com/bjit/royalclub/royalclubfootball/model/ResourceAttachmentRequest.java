package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.ResourceAttachmentKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAttachmentRequest {

    /**
     * Storage key returned by {@code POST /files/resources/presign}.
     */
    @NotBlank(message = "storage key is required.")
    @Size(max = 255, message = "storage key must be 255 characters or fewer.")
    private String storageKey;

    @Size(max = 255, message = "file name must be 255 characters or fewer.")
    private String fileName;

    @Size(max = 120, message = "content type must be 120 characters or fewer.")
    private String contentType;

    private Long sizeBytes;

    private ResourceAttachmentKind kind;

    @Size(max = 300, message = "caption must be 300 characters or fewer.")
    private String caption;

    @Size(max = 300, message = "bangla caption must be 300 characters or fewer.")
    private String captionBn;

    private Integer sortOrder;
}
