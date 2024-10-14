package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcNature;
import com.bjit.royalclub.royalclubfootball.model.account.AcNatureRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcNatureResponse;

import java.util.List;

public interface AcNatureService {

    /**
     * Retrieve all AcNature records.
     *
     * @return list of AcNatureResponse
     */
    List<AcNatureResponse> getAcNatures();

    /**
     * Retrieve an AcNature by ID.
     *
     * @param id the ID of the AcNature
     * @return AcNature entity
     */
    AcNature getAcNatureById(Long id);

    /**
     * Convert AcNature entity to AcNatureResponse.
     *
     * @param entity AcNature entity
     * @return AcNatureResponse DTO
     */
    AcNatureResponse convertToResponse(AcNature entity);

    /**
     * Get an AcNatureResponse by ID.
     *
     * @param id ID of the AcNature
     * @return AcNatureResponse DTO
     */
    AcNatureResponse getAcNatureResponse(Long id);

    /**
     * Save a new AcNature record.
     *
     * @param request AcNatureRequest DTO
     * @return ID of the newly created AcNature
     */
    Long saveAcNature(AcNatureRequest request);

    /**
     * Update an existing AcNature record.
     *
     * @param id      ID of the AcNature to update
     * @param request AcNatureRequest DTO
     * @return ID of the updated AcNature
     */
    Long updateAcNature(Long id, AcNatureRequest request);

    /**
     * Delete an AcNature by ID.
     *
     * @param id ID of the AcNature to delete
     */
    void deleteAcNature(Long id);
}
