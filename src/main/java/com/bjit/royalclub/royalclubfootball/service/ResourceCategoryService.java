package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.ResourceCategoryRequest;
import com.bjit.royalclub.royalclubfootball.model.ResourceCategoryResponse;

import java.util.List;

public interface ResourceCategoryService {

    List<ResourceCategoryResponse> categories();

    ResourceCategoryResponse getById(Long id);

    ResourceCategoryResponse save(ResourceCategoryRequest request);

    ResourceCategoryResponse update(Long id, ResourceCategoryRequest request);

    void delete(Long id);
}
