package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherType;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherTypeResponse;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcVoucherTypeServiceImpl implements AcVoucherTypeService {
    private final AcVoucherTypeRepository repository;


    @Override
    public List<AcVoucherTypeResponse> getAcVoucherTypes() {
        return repository.findAll().stream().map(this::convertToResponse).toList();
    }

    private AcVoucherTypeResponse convertToResponse(AcVoucherType entity) {
        return AcVoucherTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .alias(entity.getAlias())
                .description(entity.getDescription())
                .isDefault(entity.isDefault())
                .build();
    }

}
