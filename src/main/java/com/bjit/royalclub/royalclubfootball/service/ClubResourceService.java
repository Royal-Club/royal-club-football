package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.enums.ResourceContentType;
import com.bjit.royalclub.royalclubfootball.enums.ResourceStatus;
import com.bjit.royalclub.royalclubfootball.model.ClubResourceRequest;
import com.bjit.royalclub.royalclubfootball.model.ClubResourceResponse;

import java.util.List;

public interface ClubResourceService {

    /**
     * Every filter argument is optional. Callers who cannot manage resources
     * only ever receive PUBLISHED items regardless of the status filter.
     */
    List<ClubResourceResponse> resources(Long categoryId, ResourceContentType contentType,
                                         ResourceStatus status, String search);

    ClubResourceResponse getBySlug(String slug);

    ClubResourceResponse getById(Long id);

    ClubResourceResponse save(ClubResourceRequest request);

    ClubResourceResponse update(Long id, ClubResourceRequest request);

    ClubResourceResponse updateStatus(Long id, ResourceStatus status);

    void delete(Long id);

    /**
     * Records that the current player opened the resource. Silently ignored for
     * unauthenticated callers.
     */
    void recordView(Long id);
}
