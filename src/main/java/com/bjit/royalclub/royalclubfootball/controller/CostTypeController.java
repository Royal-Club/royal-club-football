package com.bjit.royalclub.royalclubfootball.controller;

import com.bjit.royalclub.royalclubfootball.model.CostTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.CostTypeResponse;
import com.bjit.royalclub.royalclubfootball.service.CostTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.CREATE_OK;
import static com.bjit.royalclub.royalclubfootball.constant.RestResponseMessage.FETCH_OK;
import static com.bjit.royalclub.royalclubfootball.util.ResponseBuilder.buildSuccessResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("cost-types")
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
}
