package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.enums.ResourceContentType;
import com.bjit.royalclub.royalclubfootball.enums.ResourceStatus;
import com.bjit.royalclub.royalclubfootball.model.ClubResourceRequest;
import com.bjit.royalclub.royalclubfootball.model.ClubResourceResponse;
import com.bjit.royalclub.royalclubfootball.model.ResourceStatusUpdateRequest;
import com.bjit.royalclub.royalclubfootball.service.ClubResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.STATUS_UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

/**
 * Club resource library. Reads are open to every signed-in member; anything
 * that changes the library is restricted to admins and coordinators.
 */
@RestController
@RequestMapping("resources")
@RequiredArgsConstructor
public class ClubResourceController {

    private static final String MANAGE_ROLES = "hasAnyRole('ADMIN', 'SUPERADMIN', 'COORDINATOR')";

    private final ClubResourceService clubResourceService;

    @GetMapping
    public ResponseEntity<Object> resources(@RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) ResourceContentType contentType,
                                            @RequestParam(required = false) ResourceStatus status,
                                            @RequestParam(required = false) String search) {
        List<ClubResourceResponse> responses = clubResourceService.resources(categoryId, contentType, status, search);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, responses, responses.size());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getResource(@PathVariable Long id) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, clubResourceService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Object> getResourceBySlug(@PathVariable String slug) {
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, clubResourceService.getBySlug(slug));
    }

    @PreAuthorize(MANAGE_ROLES)
    @PostMapping
    public ResponseEntity<Object> saveResource(@Valid @RequestBody ClubResourceRequest request) {
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, clubResourceService.save(request));
    }

    @PreAuthorize(MANAGE_ROLES)
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateResource(@PathVariable Long id,
                                                 @Valid @RequestBody ClubResourceRequest request) {
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, clubResourceService.update(id, request));
    }

    @PreAuthorize(MANAGE_ROLES)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Object> updateResourceStatus(@PathVariable Long id,
                                                       @Valid @RequestBody ResourceStatusUpdateRequest request) {
        return buildSuccessResponse(HttpStatus.OK, STATUS_UPDATE_OK,
                clubResourceService.updateStatus(id, request.getStatus()));
    }

    @PreAuthorize(MANAGE_ROLES)
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteResource(@PathVariable Long id) {
        clubResourceService.delete(id);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }

    /**
     * Fire-and-forget read receipt sent when a player opens a resource.
     */
    @PostMapping("/{id}/view")
    public ResponseEntity<Object> recordView(@PathVariable Long id) {
        clubResourceService.recordView(id);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK);
    }
}
