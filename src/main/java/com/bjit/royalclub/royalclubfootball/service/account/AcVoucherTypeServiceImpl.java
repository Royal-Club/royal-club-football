package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherType;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherTypeRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherTypeResponse;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherDetailRepository;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_VOUCHER_TYPE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AcVoucherTypeServiceImpl implements AcVoucherTypeService {
    private final AcVoucherTypeRepository repository;
    private final AcVoucherDetailRepository acVoucherDetailRepository;

    @Override
    public List<AcVoucherTypeResponse> getAcVoucherTypes() {
        return repository.findAll().stream().map(this::convertToResponse).toList();
    }

    @Override
    public AcVoucherType getAcVoucherTypeById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AC_VOUCHER_TYPE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    public AcVoucherTypeResponse getAcVoucherTypeResponse(Long id) {
        AcVoucherType acVoucherType = getAcVoucherTypeById(id);
        return convertToResponse(acVoucherType);
    }

    @Override
    @Transactional
    public Long saveAcVoucherType(AcVoucherTypeRequest request) {
        AcVoucherType acVoucherType = new AcVoucherType();
        mapRequestToEntity(request, acVoucherType);
        return repository.save(acVoucherType).getId();
    }

    @Override
    @Transactional
    public Long updateAcVoucherType(Long id, AcVoucherTypeRequest request) {
        AcVoucherType existingVoucherType = getAcVoucherTypeById(id);
        mapRequestToEntity(request, existingVoucherType);
        return repository.save(existingVoucherType).getId();
    }

    @Override
    @Transactional
    public void deleteAcVoucherType(Long id) {
        AcVoucherType acVoucherType = getAcVoucherTypeById(id);
        repository.delete(acVoucherType);
    }

    private void mapRequestToEntity(AcVoucherTypeRequest request, AcVoucherType acVoucherType) {
        acVoucherType.setName(request.getName());
        acVoucherType.setAlias(request.getAlias());
        acVoucherType.setDescription(request.getDescription());
        acVoucherType.setAcTransactionType(request.getAcTransactionType());
        acVoucherType.setDefault(request.isDefault());
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
