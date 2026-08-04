package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.ResourceCategory;
import com.bjit.royalclub.royalclubfootball.enums.ResourceStatus;
import com.bjit.royalclub.royalclubfootball.exception.BadRequestException;
import com.bjit.royalclub.royalclubfootball.model.ResourceCategoryRequest;
import com.bjit.royalclub.royalclubfootball.model.ResourceCategoryResponse;
import com.bjit.royalclub.royalclubfootball.repository.ClubResourceRepository;
import com.bjit.royalclub.royalclubfootball.repository.ResourceCategoryRepository;
import com.bjit.royalclub.royalclubfootball.util.CurrentUserUtil;
import com.bjit.royalclub.royalclubfootball.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.RESOURCE_CATEGORY_HAS_RESOURCES;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.RESOURCE_CATEGORY_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ResourceCategoryServiceImpl implements ResourceCategoryService {

    private final ResourceCategoryRepository resourceCategoryRepository;
    private final ClubResourceRepository clubResourceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ResourceCategoryResponse> categories() {
        return resourceCategoryRepository.findAllOrdered()
                .stream().map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceCategoryResponse getById(Long id) {
        return convertToDto(getCategoryById(id));
    }

    @Override
    @Transactional
    public ResourceCategoryResponse save(ResourceCategoryRequest request) {
        String slug = SlugUtil.uniqueSlug(
                SlugUtil.toSlug(request.getName(), "category"),
                resourceCategoryRepository::existsBySlug);

        ResourceCategory category = ResourceCategory.builder()
                .name(request.getName().trim())
                .nameBn(trimToNull(request.getNameBn()))
                .slug(slug)
                .description(trimToNull(request.getDescription()))
                .icon(trimToNull(request.getIcon()))
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isActive(request.getActive() == null || request.getActive())
                .build();

        return convertToDto(resourceCategoryRepository.save(category));
    }

    @Override
    @Transactional
    public ResourceCategoryResponse update(Long id, ResourceCategoryRequest request) {
        ResourceCategory category = getCategoryById(id);
        category.setName(request.getName().trim());
        category.setNameBn(trimToNull(request.getNameBn()));
        category.setDescription(trimToNull(request.getDescription()));
        category.setIcon(trimToNull(request.getIcon()));
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }
        return convertToDto(resourceCategoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ResourceCategory category = getCategoryById(id);
        if (clubResourceRepository.countByCategoryId(id) > 0) {
            throw new BadRequestException(RESOURCE_CATEGORY_HAS_RESOURCES, HttpStatus.CONFLICT);
        }
        resourceCategoryRepository.delete(category);
    }

    private ResourceCategory getCategoryById(Long id) {
        return resourceCategoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(RESOURCE_CATEGORY_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    private ResourceCategoryResponse convertToDto(ResourceCategory category) {
        return ResourceCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .nameBn(category.getNameBn())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .sortOrder(category.getSortOrder())
                .active(category.isActive())
                .resourceCount(visibleResourceCount(category.getId()))
                .build();
    }

    /**
     * Players see a count of what they can actually open, so the sidebar badge
     * never promises drafts they cannot read.
     */
    private long visibleResourceCount(Long categoryId) {
        if (CurrentUserUtil.hasAnyRole("ADMIN", "SUPERADMIN", "COORDINATOR")) {
            return clubResourceRepository.countByCategoryId(categoryId);
        }
        return clubResourceRepository.countByCategoryIdAndStatus(categoryId, ResourceStatus.PUBLISHED);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
