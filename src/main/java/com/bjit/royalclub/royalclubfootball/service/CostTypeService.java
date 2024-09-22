package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.CostType;
import com.bjit.royalclub.royalclubfootball.model.CostTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.CostTypeResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CostTypeService {
    @Transactional
    void saveCostType(CostTypeRequest costTypeRequest);

    List<CostTypeResponse> getAllCostType();

    CostTypeResponse getByCostId(Long id);

    CostType getCostTypeEntity(Long id);

    void updateStatus(Long id, boolean isActive);

    CostTypeResponse getByCostType(CostType costType);

    CostTypeResponse update(Long id, CostTypeRequest costTypeRequest);
}
