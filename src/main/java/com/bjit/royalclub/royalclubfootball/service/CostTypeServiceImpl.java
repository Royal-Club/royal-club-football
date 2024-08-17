package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.CostType;
import com.bjit.royalclub.royalclubfootball.exception.CostTypeServiceException;
import com.bjit.royalclub.royalclubfootball.model.CostTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.CostTypeResponse;
import com.bjit.royalclub.royalclubfootball.repository.CostTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.COST_TYPE_ALREADY_EXISTS;

@Service
@RequiredArgsConstructor
public class CostTypeServiceImpl implements CostTypeService {
    private final CostTypeRepository costTypeRepository;

    @Override
    public void saveCostType(CostTypeRequest costTypeRequest) {

        costTypeRepository.findByName(costTypeRequest.getName()).orElseThrow(() ->
                new CostTypeServiceException(COST_TYPE_ALREADY_EXISTS, HttpStatus.CONFLICT));

        CostType costType = CostType.builder()
                .name(costTypeRequest.getName())
                .description(costTypeRequest.getDescription())
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();
        costTypeRepository.save(costType);

    }

    @Override
    public List<CostTypeResponse> getAllCostType() {
        return costTypeRepository.findAll().stream().map(this::convertToCostResponse).toList();
    }

    private CostTypeResponse convertToCostResponse(CostType costType) {
        return CostTypeResponse.builder()
                .id(costType.getId())
                .name(costType.getName())
                .description(costType.getDescription())
                .build();
    }
}
