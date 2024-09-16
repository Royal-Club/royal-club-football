package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcChart;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherType;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.account.AcChartResponse;
import com.bjit.royalclub.royalclubfootball.repository.account.AcChartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_CHART_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_VOUCHER_TYPE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AcChartServiceImpl implements AcChartService {
    private final AcChartRepository repository;
    private final AcNatureService acNatureService;


    @Override
    public List<AcChartResponse> getAcCharts() {
        return repository.findAll().stream().map(this::convertToResponse).toList();
    }

    private AcChartResponse convertToResponse(AcChart entity) {
        AcChartResponse acChartResponse = AcChartResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .natureNo(entity.getAcNature().getId())
                .nature(acNatureService.convertToResponse(entity.getAcNature()))
                .build();

        if (!ObjectUtils.isEmpty(entity.getAcChart())) {
            acChartResponse.setParentNo(entity.getAcChart().getId());
            acChartResponse.setParent(convertToResponse(entity.getAcChart()));
        }

        return acChartResponse;
    }


    @Override
    public AcChart getAcChartById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AC_CHART_NOT_FOUND,
                        HttpStatus.NOT_FOUND));
    }

    @Override
    public AcChartResponse getAcChartResponse(AcChart acChart) {
        return convertToResponse(acChart);
    }

}
