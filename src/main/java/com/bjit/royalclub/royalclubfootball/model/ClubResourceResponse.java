package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.ResourceContentType;
import com.bjit.royalclub.royalclubfootball.enums.ResourceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubResourceResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryNameBn;
    private String categorySlug;
    private String categoryIcon;

    private String title;
    private String titleBn;
    private String slug;
    private String summary;
    private String summaryBn;
    private String body;
    private String bodyBn;

    private ResourceContentType contentType;
    private ResourceStatus status;
    private String coverImageKey;
    private String coverImageUrl;
    private String externalUrl;
    private String metadata;

    private boolean pinned;
    private Integer sortOrder;
    private Long viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    /**
     * True when the resource carries a Bangla translation, so the frontend can
     * show the language toggle only where it is useful.
     */
    private boolean bilingual;

    private List<ResourceAttachmentResponse> attachments;
}
