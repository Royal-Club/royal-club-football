package com.bjit.royalclub.royalclubfootball.model;

import com.bjit.royalclub.royalclubfootball.enums.ResourceContentType;
import com.bjit.royalclub.royalclubfootball.enums.ResourceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubResourceRequest {

    @NotNull(message = "category is required.")
    private Long categoryId;

    @NotBlank(message = "title is required.")
    @Size(max = 200, message = "title must be 200 characters or fewer.")
    private String title;

    @Size(max = 200, message = "bangla title must be 200 characters or fewer.")
    private String titleBn;

    @Size(max = 500, message = "summary must be 500 characters or fewer.")
    private String summary;

    @Size(max = 500, message = "bangla summary must be 500 characters or fewer.")
    private String summaryBn;

    /**
     * Markdown body. Optional for VIDEO/LINK/DOCUMENT resources that carry
     * their content in {@link #externalUrl} or an attachment instead.
     */
    private String body;

    private String bodyBn;

    @NotNull(message = "content type is required.")
    private ResourceContentType contentType;

    /**
     * Omitted on create means DRAFT.
     */
    private ResourceStatus status;

    @Size(max = 255, message = "cover image key must be 255 characters or fewer.")
    private String coverImageKey;

    @Size(max = 500, message = "external url must be 500 characters or fewer.")
    private String externalUrl;

    private String metadata;

    private Boolean pinned;

    private Integer sortOrder;

    /**
     * Full replacement list — attachments absent from it are removed.
     */
    @Valid
    private List<ResourceAttachmentRequest> attachments;
}
