package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcNature;
import com.bjit.royalclub.royalclubfootball.model.account.AcNatureResponse;
import com.bjit.royalclub.royalclubfootball.repository.account.AcNatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcNatureServiceImpl implements AcNatureService {
    private final AcNatureRepository repository;


    @Override
    public List<AcNatureResponse> getAcNatures() {
        return repository.findAll().stream().map(this::convertToResponse).toList();
    }

    public AcNatureResponse convertToResponse(AcNature entity) {
        return AcNatureResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .type(entity.getType())
                .slNo(entity.getSlNo())
                .build();
    }

}
