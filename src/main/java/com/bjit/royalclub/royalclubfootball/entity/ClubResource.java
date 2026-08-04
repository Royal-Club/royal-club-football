package com.bjit.royalclub.royalclubfootball.entity;

import com.bjit.royalclub.royalclubfootball.entity.audit.AuditBase;
import com.bjit.royalclub.royalclubfootball.enums.ResourceContentType;
import com.bjit.royalclub.royalclubfootball.enums.ResourceStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single item in the club resource library — a written guide, a formation
 * plan, a video or a downloadable document.
 * <p>
 * Named {@code ClubResource} rather than {@code Resource} to avoid colliding
 * with {@code org.springframework.core.io.Resource} in this codebase.
 */
@Entity
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "resource")
public class ClubResource extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private ResourceCategory category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "title_bn")
    private String titleBn;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "summary")
    private String summary;

    @Column(name = "summary_bn")
    private String summaryBn;

    /**
     * Markdown. Rendered by the frontend with react-markdown + remark-gfm.
     */
    @Column(name = "body", columnDefinition = "LONGTEXT")
    private String body;

    @Column(name = "body_bn", columnDefinition = "LONGTEXT")
    private String bodyBn;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ResourceContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResourceStatus status;

    @Column(name = "cover_image_key")
    private String coverImageKey;

    /**
     * YouTube or external page URL, used by VIDEO and LINK resources.
     */
    @Column(name = "external_url")
    private String externalUrl;

    /**
     * Free-form JSON reserved for structured payloads such as interactive
     * formation coordinates.
     */
    @Column(name = "metadata", columnDefinition = "LONGTEXT")
    private String metadata;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private List<ResourceAttachment> attachments = new ArrayList<>();
}
