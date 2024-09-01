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
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.COST_TYPE_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CostTypeServiceImpl implements CostTypeService {
    private final CostTypeRepository costTypeRepository;

    @Override
    public void saveCostType(CostTypeRequest costTypeRequest) {

        costTypeRepository.findByName(costTypeRequest.getName())
                .ifPresent(costType -> {
                    throw new CostTypeServiceException(COST_TYPE_ALREADY_EXISTS, HttpStatus.CONFLICT);
                });

        CostType costType = CostType.builder()
                .name(costTypeRequest.getName())
                .description(costTypeRequest.getDescription())
                /*As it is admin api, so always will be true*/
                .isActive(true)
                .createdDate(LocalDateTime.now())
                .build();
        costTypeRepository.save(costType);

    }

    @Override
    public List<CostTypeResponse> getCostTypes() {
        return costTypeRepository.findAll().stream().map(this::convertToCostResponse).toList();
    }

    @Override
    public CostTypeResponse getByCostId(Long id) {
        CostType costType = costTypeRepository.findById(id)
                .orElseThrow(() -> new CostTypeServiceException(COST_TYPE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        return convertToCostResponse(costType);
    }

    @Override
    public void updateStatus(Long id, boolean isActive) {
        CostType costType = costTypeRepository.findById(id)
                .orElseThrow(() -> new CostTypeServiceException(COST_TYPE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));
        costType.setActive(isActive);
        costType.setUpdatedDate(LocalDateTime.now());
        costTypeRepository.save(costType);
    }

    @Override
    public CostTypeResponse update(Long id, CostTypeRequest costTypeRequest) {
        CostType costType = costTypeRepository.findById(id)
                .orElseThrow(() -> new CostTypeServiceException(COST_TYPE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        costType.setName(costTypeRequest.getName());
        costType.setDescription(costTypeRequest.getDescription());
        costType.setUpdatedDate(LocalDateTime.now());
        costTypeRepository.save(costType);
        return convertToCostResponse(costType);
    }

    private CostTypeResponse convertToCostResponse(CostType costType) {
        return CostTypeResponse.builder()
                .id(costType.getId())
                .name(costType.getName())
                .description(costType.getDescription())
                .isActive(costType.isActive())
                .build();
    }
}
