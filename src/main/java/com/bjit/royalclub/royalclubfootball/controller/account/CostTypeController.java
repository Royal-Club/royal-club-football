package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.CostTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.CostTypeResponse;
import com.bjit.royalclub.royalclubfootball.service.CostTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.*;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("cost-types")
@PreAuthorize("hasAnyRole('ADMIN')")
public class CostTypeController {

    private final CostTypeService costTypeService;

    /**
     * Create a new CostType
     */
    @PostMapping
    public ResponseEntity<Object> saveCostType(@Valid @RequestBody CostTypeRequest costTypeRequest) {
        costTypeService.saveCostType(costTypeRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }

    /**
     * Retrieve all CostTypes
     */
    @GetMapping
    public ResponseEntity<Object> getAllCostType() {
        List<CostTypeResponse> costTypeResponses = costTypeService.getAllCostType();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, costTypeResponses);
    }

    /**
     * Retrieve a CostType by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getCostTypeById(@PathVariable Long id) {
        CostTypeResponse costType = costTypeService.getByCostId(id);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, costType);
    }

    /**
     * Update the status of a CostType
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Object> updateCostTypeStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        costTypeService.updateStatus(id, isActive);
        return buildSuccessResponse(HttpStatus.OK, STATUS_UPDATE_OK);
    }

    /**
     * Update an existing CostType by ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCostType(@PathVariable Long id, @Valid @RequestBody CostTypeRequest costTypeRequest) {
        CostTypeResponse updatedCostType = costTypeService.update(id, costTypeRequest);
        return buildSuccessResponse(HttpStatus.OK, UPDATE_OK, updatedCostType);
    }
}
