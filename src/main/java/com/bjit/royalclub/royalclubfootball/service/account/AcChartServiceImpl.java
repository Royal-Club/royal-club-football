package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcChart;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherDetail;
import com.bjit.royalclub.royalclubfootball.exception.BadRequestException;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.account.AcChartRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcChartResponse;
import com.bjit.royalclub.royalclubfootball.repository.account.AcChartRepository;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_CHART_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_CHART_HAS_VOUCHER;

@Service
@RequiredArgsConstructor
public class AcChartServiceImpl implements AcChartService {
    private final AcChartRepository repository;
    private final AcVoucherDetailRepository acVoucherDetailRepository;
    private final AcNatureService acNatureService;

    @Override
    public List<AcChartResponse> getAcCharts() {
        return repository.findAll().stream().map(this::convertToResponse).toList();
    }

    @Override
    public AcChartResponse getAcChartResponse(AcChart acChart) {
        return convertToResponse(acChart);
    }

    @Override
    public AcChart getAcChartById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AC_CHART_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    /**
     * Save a new chart.
     */
    @Override
    public Long saveChart(AcChartRequest request) {
        AcChart acChart = new AcChart();
        mapRequestToEntity(request, acChart);
        return repository.save(acChart).getId();
    }

    /**
     * Update an existing chart by ID.
     */
    @Override
    public Long updateChart(Long id, AcChartRequest request) {
        AcChart existingChart = getAcChartById(id);
        mapRequestToEntity(request, existingChart);
        return repository.save(existingChart).getId();
    }

    /**
     * Delete a chart by ID, ensuring that no vouchers are associated with it.
     */
    @Override
    public void deleteChart(Long id) {
        AcChart existingChart = getAcChartById(id);
        if (acVoucherDetailRepository.existsByAcChart(existingChart)) {
            throw new BadRequestException(AC_CHART_HAS_VOUCHER, HttpStatus.BAD_REQUEST);
        }
        repository.delete(existingChart);
    }

    /**
     * Maps AcChartRequest to AcChart entity.
     */
    private void mapRequestToEntity(AcChartRequest request, AcChart acChart) {
        acChart.setName(request.getName());
        acChart.setCode(request.getCode());
        acChart.setDescription(request.getDescription());
        acChart.setAcNature(acNatureService.getAcNatureById(request.getNatureId()));
        if (request.getParentId() != null) {
            AcChart parentChart = getAcChartById(request.getParentId());
            acChart.setAcChart(parentChart);
        }
    }

    /**
     * Converts an AcChart entity to AcChartResponse.
     */
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
}
