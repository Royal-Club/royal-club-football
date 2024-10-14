package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcNature;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.account.AcNatureRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcNatureResponse;
import com.bjit.royalclub.royalclubfootball.repository.account.AcNatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_NATURE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AcNatureServiceImpl implements AcNatureService {

    private final AcNatureRepository repository;

    @Override
    public List<AcNatureResponse> getAcNatures() {
        return repository.findAll().stream().map(this::convertToResponse).toList();
    }

    @Override
    public AcNatureResponse getAcNatureResponse(Long id) {
        AcNature acNature = getAcNatureById(id);
        return convertToResponse(acNature);
    }

    @Override
    public AcNature getAcNatureById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AC_NATURE_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    @Override
    public Long saveAcNature(AcNatureRequest request) {
        AcNature acNature = new AcNature();
        mapRequestToEntity(request, acNature);
        return repository.save(acNature).getId();
    }

    @Override
    public Long updateAcNature(Long id, AcNatureRequest request) {
        AcNature existingAcNature = getAcNatureById(id);
        mapRequestToEntity(request, existingAcNature);
        return repository.save(existingAcNature).getId();
    }

    @Override
    public void deleteAcNature(Long id) {
        AcNature acNature = getAcNatureById(id);
        repository.delete(acNature);
    }

    private void mapRequestToEntity(AcNatureRequest request, AcNature acNature) {
        acNature.setName(request.getName());
        acNature.setCode(request.getCode());
        acNature.setType(request.getType());
        acNature.setDescription(request.getDescription());
        acNature.setSlNo(request.getSlNo());
    }

    @Override
    public AcNatureResponse convertToResponse(AcNature entity) {
        return AcNatureResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .type(entity.getType())
                .description(entity.getDescription())
                .slNo(entity.getSlNo())
                .build();
    }
}
