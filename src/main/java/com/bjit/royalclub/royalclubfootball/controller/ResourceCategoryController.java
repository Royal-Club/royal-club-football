package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.ResourceCategoryRequest;
import com.bjit.royalclub.royalclubfootball.model.ResourceCategoryResponse;
import com.bjit.royalclub.royalclubfootball.service.ResourceCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequestMapping("resource-categories")
@RequiredArgsConstructor
public class ResourceCategoryController {

    private static final String MANAGE_ROLES = "hasAnyRole('ADMIN', 'SUPERADMIN', 'COORDINATOR')";

    private final ResourceCategoryService resourceCategoryService;

    @GetMapping
    public ResponseEntity<Object> categories() {
        List<ResourceCategoryResponse> responses = resourceCategoryService.categories();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, responses, responses.size());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getCategory(@PathVariable Long id) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, resourceCategoryService.getById(id));
    }

    @PreAuthorize(MANAGE_ROLES)
    @PostMapping
    public ResponseEntity<Object> saveCategory(@Valid @RequestBody ResourceCategoryRequest request) {
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, resourceCategoryService.save(request));
    }

    @PreAuthorize(MANAGE_ROLES)
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCategory(@PathVariable Long id,
                                                 @Valid @RequestBody ResourceCategoryRequest request) {
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, resourceCategoryService.update(id, request));
    }

    @PreAuthorize(MANAGE_ROLES)
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCategory(@PathVariable Long id) {
        resourceCategoryService.delete(id);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }
}
