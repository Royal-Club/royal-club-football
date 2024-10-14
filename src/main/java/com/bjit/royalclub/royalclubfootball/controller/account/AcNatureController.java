package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.account.AcNatureRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcNatureResponse;
import com.bjit.royalclub.royalclubfootball.service.account.AcNatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.DELETE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("ac/natures")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AcNatureController {

    private final AcNatureService service;

    /**
     * Get all account natures.
     *
     * @return list of all AcNatureResponse
     */
    @GetMapping
    public ResponseEntity<Object> getAcNatures() {
        List<AcNatureResponse> natures = service.getAcNatures();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, natures);
    }

    /**
     * Get a specific account nature by ID.
     *
     * @param id ID of the AcNature to retrieve
     * @return AcNatureResponse for the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getAcNatureById(@PathVariable Long id) {
        AcNatureResponse nature = service.getAcNatureResponse(id);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, nature);
    }

    /**
     * Create a new account nature.
     *
     * @param request the AcNatureRequest object containing details for the new nature
     * @return the ID of the newly created AcNature
     */
    @PostMapping
    public ResponseEntity<Object> createAcNature(@Valid @RequestBody AcNatureRequest request) {
        Long id = service.saveAcNature(request);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK, id);
    }

    /**
     * Update an existing account nature.
     *
     * @param id      the ID of the AcNature to update
     * @param request the AcNatureRequest object containing updated details
     * @return the ID of the updated AcNature
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAcNature(@PathVariable Long id, @Valid @RequestBody AcNatureRequest request) {
        Long updatedId = service.updateAcNature(id, request);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, updatedId);
    }

    /**
     * Delete an account nature by ID.
     *
     * @param id the ID of the AcNature to delete
     * @return success message for the deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAcNature(@PathVariable Long id) {
        service.deleteAcNature(id);
        return buildSuccessResponse(HttpStatus.OK, DELETE_OK);
    }
}
