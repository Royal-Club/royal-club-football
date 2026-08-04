package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.ClubResource;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.ResourceAttachment;
import com.bjit.royalclub.royalclubfootball.entity.ResourceCategory;
import com.bjit.royalclub.royalclubfootball.entity.ResourceView;
import com.bjit.royalclub.royalclubfootball.enums.ResourceAttachmentKind;
import com.bjit.royalclub.royalclubfootball.enums.ResourceContentType;
import com.bjit.royalclub.royalclubfootball.enums.ResourceStatus;
import com.bjit.royalclub.royalclubfootball.exception.BadRequestException;
import com.bjit.royalclub.royalclubfootball.model.ClubResourceRequest;
import com.bjit.royalclub.royalclubfootball.model.ClubResourceResponse;
import com.bjit.royalclub.royalclubfootball.model.ResourceAttachmentRequest;
import com.bjit.royalclub.royalclubfootball.model.ResourceAttachmentResponse;
import com.bjit.royalclub.royalclubfootball.repository.ClubResourceRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import com.bjit.royalclub.royalclubfootball.repository.ResourceCategoryRepository;
import com.bjit.royalclub.royalclubfootball.repository.ResourceViewRepository;
import com.bjit.royalclub.royalclubfootball.storage.resourcefile.ResourceFileStorageProvider;
import com.bjit.royalclub.royalclubfootball.util.CurrentUserUtil;
import com.bjit.royalclub.royalclubfootball.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.CLUB_RESOURCE_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.RESOURCE_CATEGORY_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.RESOURCE_CONTENT_IS_REQUIRED;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.RESOURCE_EXTERNAL_URL_IS_REQUIRED;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClubResourceServiceImpl implements ClubResourceService {

    private static final String FILE_URL_PREFIX = "/files/resources/";
    private static final String[] MANAGER_ROLES = {"ADMIN", "SUPERADMIN", "COORDINATOR"};

    private final ClubResourceRepository clubResourceRepository;
    private final ResourceCategoryRepository resourceCategoryRepository;
    private final ResourceViewRepository resourceViewRepository;
    private final PlayerRepository playerRepository;
    private final ResourceFileStorageProvider resourceFileStorageProvider;

    @Override
    @Transactional(readOnly = true)
    public List<ClubResourceResponse> resources(Long categoryId, ResourceContentType contentType,
                                                ResourceStatus status, String search) {
        List<ClubResource> candidates = loadVisibleResources(status);
        String needle = search == null || search.isBlank() ? null : search.trim().toLowerCase(Locale.ENGLISH);

        return candidates.stream()
                .filter(resource -> categoryId == null || categoryId.equals(resource.getCategory().getId()))
                .filter(resource -> contentType == null || contentType == resource.getContentType())
                .filter(resource -> needle == null || matches(resource, needle))
                .map(resource -> convertToDto(resource, false))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClubResourceResponse getBySlug(String slug) {
        ClubResource resource = clubResourceRepository.findBySlugWithCategory(slug)
                .orElseThrow(() -> new BadRequestException(CLUB_RESOURCE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        assertReadable(resource);
        return convertToDto(resource, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubResourceResponse getById(Long id) {
        ClubResource resource = getResourceById(id);
        assertReadable(resource);
        return convertToDto(resource, true);
    }

    @Override
    @Transactional
    public ClubResourceResponse save(ClubResourceRequest request) {
        ResourceCategory category = getCategoryById(request.getCategoryId());
        validateContent(request);

        ResourceStatus status = request.getStatus() != null ? request.getStatus() : ResourceStatus.DRAFT;
        String slug = SlugUtil.uniqueSlug(
                SlugUtil.toSlug(request.getTitle(), "resource"),
                clubResourceRepository::existsBySlug);

        ClubResource resource = ClubResource.builder()
                .category(category)
                .title(request.getTitle().trim())
                .titleBn(trimToNull(request.getTitleBn()))
                .slug(slug)
                .summary(trimToNull(request.getSummary()))
                .summaryBn(trimToNull(request.getSummaryBn()))
                .body(trimToNull(request.getBody()))
                .bodyBn(trimToNull(request.getBodyBn()))
                .contentType(request.getContentType())
                .status(status)
                .coverImageKey(trimToNull(request.getCoverImageKey()))
                .externalUrl(trimToNull(request.getExternalUrl()))
                .metadata(trimToNull(request.getMetadata()))
                .isPinned(Boolean.TRUE.equals(request.getPinned()))
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .viewCount(0L)
                .publishedAt(status == ResourceStatus.PUBLISHED ? LocalDateTime.now() : null)
                .attachments(new ArrayList<>())
                .build();

        applyAttachments(resource, request.getAttachments());

        return convertToDto(clubResourceRepository.save(resource), true);
    }

    @Override
    @Transactional
    public ClubResourceResponse update(Long id, ClubResourceRequest request) {
        ClubResource resource = getResourceById(id);
        ResourceCategory category = getCategoryById(request.getCategoryId());
        validateContent(request);

        // The slug is deliberately left alone. Shared links stay valid even
        // when a title is corrected after publishing.
        resource.setCategory(category);
        resource.setTitle(request.getTitle().trim());
        resource.setTitleBn(trimToNull(request.getTitleBn()));
        resource.setSummary(trimToNull(request.getSummary()));
        resource.setSummaryBn(trimToNull(request.getSummaryBn()));
        resource.setBody(trimToNull(request.getBody()));
        resource.setBodyBn(trimToNull(request.getBodyBn()));
        resource.setContentType(request.getContentType());
        resource.setExternalUrl(trimToNull(request.getExternalUrl()));
        resource.setMetadata(trimToNull(request.getMetadata()));

        String newCoverKey = trimToNull(request.getCoverImageKey());
        String oldCoverKey = resource.getCoverImageKey();
        if (oldCoverKey != null && !oldCoverKey.equals(newCoverKey)) {
            deleteStoredFile(oldCoverKey);
        }
        resource.setCoverImageKey(newCoverKey);

        if (request.getPinned() != null) {
            resource.setPinned(request.getPinned());
        }
        if (request.getSortOrder() != null) {
            resource.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            applyStatus(resource, request.getStatus());
        }

        applyAttachments(resource, request.getAttachments());

        return convertToDto(clubResourceRepository.save(resource), true);
    }

    @Override
    @Transactional
    public ClubResourceResponse updateStatus(Long id, ResourceStatus status) {
        ClubResource resource = getResourceById(id);
        applyStatus(resource, status);
        return convertToDto(clubResourceRepository.save(resource), true);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ClubResource resource = getResourceById(id);

        // Drop the binaries first; orphaned blobs are invisible and never
        // cleaned up otherwise.
        deleteStoredFile(resource.getCoverImageKey());
        resource.getAttachments().forEach(attachment -> deleteStoredFile(attachment.getStorageKey()));

        clubResourceRepository.delete(resource);
    }

    @Override
    @Transactional
    public void recordView(Long id) {
        Optional<Long> playerId = CurrentUserUtil.currentPlayerId();
        if (playerId.isEmpty()) {
            return;
        }

        ClubResource resource = getResourceById(id);
        assertReadable(resource);
        clubResourceRepository.incrementViewCount(id);

        LocalDateTime now = LocalDateTime.now();
        resourceViewRepository.findByResourceIdAndPlayerId(id, playerId.get())
                .ifPresentOrElse(view -> {
                    view.setViewCount(view.getViewCount() + 1);
                    view.setLastViewedAt(now);
                    resourceViewRepository.save(view);
                }, () -> playerRepository.findById(playerId.get()).ifPresent(player ->
                        resourceViewRepository.save(newView(resource, player, now))));
    }

    private ResourceView newView(ClubResource resource, Player player, LocalDateTime now) {
        return ResourceView.builder()
                .resource(resource)
                .player(player)
                .viewCount(1L)
                .firstViewedAt(now)
                .lastViewedAt(now)
                .build();
    }

    /**
     * Managers see everything except archived items unless they ask for a
     * specific status. Everyone else only ever sees published items.
     */
    private List<ClubResource> loadVisibleResources(ResourceStatus status) {
        if (!canManage()) {
            return clubResourceRepository.findAllByStatusOrdered(ResourceStatus.PUBLISHED);
        }
        if (status != null) {
            return clubResourceRepository.findAllByStatusOrdered(status);
        }
        return clubResourceRepository.findAllOrdered().stream()
                .filter(resource -> resource.getStatus() != ResourceStatus.ARCHIVED)
                .toList();
    }

    private void assertReadable(ClubResource resource) {
        if (resource.getStatus() != ResourceStatus.PUBLISHED && !canManage()) {
            throw new BadRequestException(CLUB_RESOURCE_IS_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    private boolean canManage() {
        return CurrentUserUtil.hasAnyRole(MANAGER_ROLES);
    }

    private void applyStatus(ClubResource resource, ResourceStatus status) {
        if (status == ResourceStatus.PUBLISHED && resource.getPublishedAt() == null) {
            resource.setPublishedAt(LocalDateTime.now());
        }
        resource.setStatus(status);
    }

    /**
     * Replaces the attachment list wholesale. Rows dropped from the request are
     * removed together with their stored binary; rows still present keep their
     * identity so captions and ordering can be edited without churning ids.
     */
    private void applyAttachments(ClubResource resource, List<ResourceAttachmentRequest> requested) {
        if (requested == null) {
            return;
        }

        Set<String> keptKeys = new HashSet<>();
        requested.forEach(attachment -> keptKeys.add(attachment.getStorageKey()));

        List<ResourceAttachment> existing = resource.getAttachments();
        Map<String, ResourceAttachment> byKey = new LinkedHashMap<>();
        existing.forEach(attachment -> byKey.put(attachment.getStorageKey(), attachment));

        existing.stream()
                .filter(attachment -> !keptKeys.contains(attachment.getStorageKey()))
                .forEach(attachment -> deleteStoredFile(attachment.getStorageKey()));

        List<ResourceAttachment> rebuilt = new ArrayList<>();
        int index = 0;
        for (ResourceAttachmentRequest attachmentRequest : requested) {
            ResourceAttachment attachment = byKey.get(attachmentRequest.getStorageKey());
            if (attachment == null) {
                attachment = ResourceAttachment.builder()
                        .resource(resource)
                        .storageKey(attachmentRequest.getStorageKey())
                        .build();
            }
            attachment.setFileName(trimToNull(attachmentRequest.getFileName()));
            attachment.setContentType(trimToNull(attachmentRequest.getContentType()));
            attachment.setSizeBytes(attachmentRequest.getSizeBytes());
            attachment.setKind(attachmentRequest.getKind() != null
                    ? attachmentRequest.getKind()
                    : inferKind(attachmentRequest.getContentType()));
            attachment.setCaption(trimToNull(attachmentRequest.getCaption()));
            attachment.setCaptionBn(trimToNull(attachmentRequest.getCaptionBn()));
            attachment.setSortOrder(attachmentRequest.getSortOrder() != null
                    ? attachmentRequest.getSortOrder()
                    : index);
            rebuilt.add(attachment);
            index++;
        }

        existing.clear();
        existing.addAll(rebuilt);
    }

    private static ResourceAttachmentKind inferKind(String contentType) {
        if (contentType == null) {
            return ResourceAttachmentKind.OTHER;
        }
        if (contentType.startsWith("image/")) {
            return ResourceAttachmentKind.IMAGE;
        }
        if (contentType.startsWith("application/pdf") || contentType.startsWith("application/vnd")
                || contentType.startsWith("application/msword") || contentType.startsWith("text/")) {
            return ResourceAttachmentKind.DOCUMENT;
        }
        return ResourceAttachmentKind.OTHER;
    }

    /**
     * A resource has to carry something a player can actually consume.
     */
    private void validateContent(ClubResourceRequest request) {
        boolean hasExternalUrl = trimToNull(request.getExternalUrl()) != null;

        if (request.getContentType() == ResourceContentType.VIDEO
                || request.getContentType() == ResourceContentType.LINK) {
            if (!hasExternalUrl) {
                throw new BadRequestException(RESOURCE_EXTERNAL_URL_IS_REQUIRED, HttpStatus.BAD_REQUEST);
            }
            return;
        }

        boolean hasBody = trimToNull(request.getBody()) != null || trimToNull(request.getBodyBn()) != null;
        boolean hasAttachment = request.getAttachments() != null && !request.getAttachments().isEmpty();
        if (!hasBody && !hasAttachment && !hasExternalUrl) {
            throw new BadRequestException(RESOURCE_CONTENT_IS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
    }

    private static boolean matches(ClubResource resource, String needle) {
        return containsIgnoreCase(resource.getTitle(), needle)
                || containsIgnoreCase(resource.getTitleBn(), needle)
                || containsIgnoreCase(resource.getSummary(), needle)
                || containsIgnoreCase(resource.getSummaryBn(), needle)
                || containsIgnoreCase(resource.getBody(), needle)
                || containsIgnoreCase(resource.getBodyBn(), needle);
    }

    private static boolean containsIgnoreCase(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ENGLISH).contains(needle);
    }

    private void deleteStoredFile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            resourceFileStorageProvider.delete(key);
        } catch (Exception ex) {
            log.warn("Failed to delete resource file key={}: {}", key, ex.getMessage());
        }
    }

    private ClubResource getResourceById(Long id) {
        return clubResourceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(CLUB_RESOURCE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private ResourceCategory getCategoryById(Long id) {
        return resourceCategoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(RESOURCE_CATEGORY_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    /**
     * @param withContent list responses omit the markdown bodies and the
     *                    attachment list to keep the library payload small
     */
    private ClubResourceResponse convertToDto(ClubResource resource, boolean withContent) {
        ResourceCategory category = resource.getCategory();

        ClubResourceResponse.ClubResourceResponseBuilder builder = ClubResourceResponse.builder()
                .id(resource.getId())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .categoryNameBn(category != null ? category.getNameBn() : null)
                .categorySlug(category != null ? category.getSlug() : null)
                .categoryIcon(category != null ? category.getIcon() : null)
                .title(resource.getTitle())
                .titleBn(resource.getTitleBn())
                .slug(resource.getSlug())
                .summary(resource.getSummary())
                .summaryBn(resource.getSummaryBn())
                .contentType(resource.getContentType())
                .status(resource.getStatus())
                .coverImageKey(resource.getCoverImageKey())
                .coverImageUrl(toFileUrl(resource.getCoverImageKey()))
                .externalUrl(resource.getExternalUrl())
                .metadata(resource.getMetadata())
                .pinned(resource.isPinned())
                .sortOrder(resource.getSortOrder())
                .viewCount(resource.getViewCount())
                .publishedAt(resource.getPublishedAt())
                .createdDate(resource.getCreatedDate())
                .updatedDate(resource.getUpdatedDate())
                .bilingual(hasBanglaContent(resource));

        if (withContent) {
            builder.body(resource.getBody())
                    .bodyBn(resource.getBodyBn())
                    .attachments(resource.getAttachments().stream()
                            .map(ClubResourceServiceImpl::convertAttachmentToDto)
                            .toList());
        }

        return builder.build();
    }

    private static boolean hasBanglaContent(ClubResource resource) {
        return resource.getBodyBn() != null || resource.getTitleBn() != null || resource.getSummaryBn() != null;
    }

    private static ResourceAttachmentResponse convertAttachmentToDto(ResourceAttachment attachment) {
        return ResourceAttachmentResponse.builder()
                .id(attachment.getId())
                .storageKey(attachment.getStorageKey())
                .url(toFileUrl(attachment.getStorageKey()))
                .fileName(attachment.getFileName())
                .contentType(attachment.getContentType())
                .sizeBytes(attachment.getSizeBytes())
                .kind(attachment.getKind())
                .caption(attachment.getCaption())
                .captionBn(attachment.getCaptionBn())
                .sortOrder(attachment.getSortOrder())
                .build();
    }

    private static String toFileUrl(String key) {
        return key == null || key.isBlank() ? null : FILE_URL_PREFIX + key;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
