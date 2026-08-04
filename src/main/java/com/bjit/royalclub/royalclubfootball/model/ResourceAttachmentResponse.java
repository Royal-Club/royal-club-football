package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.ResourceAttachmentKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceAttachmentResponse {
    private Long id;
    private String storageKey;
    /**
     * Path the frontend can request the binary from, relative to the API host.
     */
    private String url;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private ResourceAttachmentKind kind;
    private String caption;
    private String captionBn;
    private Integer sortOrder;
}
