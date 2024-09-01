package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.CostTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.CostTypeResponse;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CostTypeService {
    @Transactional
    void saveCostType(CostTypeRequest costTypeRequest);

    List<CostTypeResponse> getCostTypes();

    CostTypeResponse getByCostId(Long id);

    void updateStatus(Long id, boolean isActive);

    CostTypeResponse update(Long id, CostTypeRequest costTypeRequest);
}
