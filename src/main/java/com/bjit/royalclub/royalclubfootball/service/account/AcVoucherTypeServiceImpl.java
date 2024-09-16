package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherType;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherTypeResponse;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_VOUCHER_TYPE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AcVoucherTypeServiceImpl implements AcVoucherTypeService {
    private final AcVoucherTypeRepository repository;


    @Override
    public List<AcVoucherTypeResponse> getAcVoucherTypes() {
        return repository.findAll().stream().map(this::convertToResponse).toList();
    }

    @Override
    public AcVoucherType getAcVoucherTypeById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AC_VOUCHER_TYPE_NOT_FOUND,
                        HttpStatus.NOT_FOUND));
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
