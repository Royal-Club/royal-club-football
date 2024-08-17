package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.CostTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.CostTypeResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CostTypeService {
    @Transactional
    void saveCostType(CostTypeRequest costTypeRequest);

    List<CostTypeResponse> getAllCostType();
}
