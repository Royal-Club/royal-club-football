package com.bjit.royalclub.royalclubfootball.controller.account;

import com.bjit.royalclub.royalclubfootball.model.CostTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.CostTypeResponse;
import com.bjit.royalclub.royalclubfootball.service.CostTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.STATUS_UPDATE_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("cost-types")
//@PreAuthorize("hasAnyRole('ADMIN')")
public class CostTypeController {

    private final CostTypeService costTypeService;

    @PostMapping
    public ResponseEntity<Object> saveCostType(@Valid @RequestBody CostTypeRequest costTypeRequest) {
        costTypeService.saveCostType(costTypeRequest);
        return buildSuccessResponse(HttpStatus.CREATED, CREATE_OK);
    }

    @GetMapping
    public ResponseEntity<Object> getAllCostType() {
        List<CostTypeResponse> costTypeResponses = costTypeService.getAllCostType();
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, costTypeResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getCostTypeById(@PathVariable Long id) {
        CostTypeResponse costType = costTypeService.getByCostId(id);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, costType);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Object> updateCostTypeStatus(@PathVariable Long id, @RequestParam boolean isActive) {
        costTypeService.updateStatus(id, isActive);
        return buildSuccessResponse(HttpStatus.OK, STATUS_UPDATE_OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateCostType(@PathVariable Long id, @Valid @RequestBody CostTypeRequest costTypeRequest) {
        CostTypeResponse updatedCostType = costTypeService.update(id, costTypeRequest);
        return buildSuccessResponse(HttpStatus.OK, FETCH_OK, updatedCostType);
    }
}
