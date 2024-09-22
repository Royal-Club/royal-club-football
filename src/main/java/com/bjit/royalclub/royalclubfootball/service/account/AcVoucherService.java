package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcChart;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucher;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherDetail;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherType;
import com.bjit.royalclub.royalclubfootball.exception.BadrRequestException;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherDetailRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherDetailResponse;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherResponse;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherDetailRepository;
import com.bjit.royalclub.royalclubfootball.repository.account.AcVoucherRepository;
import com.bjit.royalclub.royalclubfootball.service.PlayerService;
import com.bjit.royalclub.royalclubfootball.util.RandomUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Objects;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_VOUCHER_DR_CR_AMOUNT_NOT_SAME;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_VOUCHER_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.security.util.SecurityUtil.getLoggedInPlayer;

@RequiredArgsConstructor
@Service
@Slf4j
public class AcVoucherService {
    private final AcVoucherRepository acVoucherRepository;
    private final AcVoucherDetailRepository acVoucherDetailRepository;
    private final AcVoucherTypeService acVoucherTypeService;
    private final AcChartService acChartService;
    private final PlayerService playerService;
    private final AcChartService accountChartService;
    private final AcCollectionService acCollectionService;


    public List<AcVoucherResponse> getAllAcVouchers(Boolean isDetailsResponse) {
        return acVoucherRepository.findAll().stream().map(
                entity -> getAcVoucherResponse(entity.getId(), isDetailsResponse)
        ).toList();
    }

    public AcVoucherResponse getAcVoucherResponse(Long id, Boolean isDetailsResponse) {
        return getAcVoucherResponse(getAcVoucher(id), isDetailsResponse);
    }

    public AcVoucher getAcVoucher(Long id) {
        return acVoucherRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(AC_VOUCHER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    public AcVoucherResponse getAcVoucherResponse(AcVoucher acVoucher,
                                                  Boolean isDetailsResponse) {
        return convertToDto(acVoucher, isDetailsResponse);
    }

    private AcVoucherResponse convertToDto(AcVoucher acVoucher, Boolean isDetailsResponse) {
        AcVoucherResponse acVoucherResponse = new AcVoucherResponse();

        acVoucherResponse.setId(acVoucher.getId());
        acVoucherResponse.setAmount(acVoucher.getAmount());
        acVoucherResponse.setNarration(acVoucher.getNarration());
        acVoucherResponse.setCode(acVoucher.getCode());
        acVoucherResponse.setVoucherDate(acVoucher.getVoucherDate());
        acVoucherResponse.setAmount(acVoucher.getAmount());
        acVoucherResponse.setVoucherType(acVoucher.getVoucherType());
        acVoucherResponse.setPostDate(acVoucher.getPostDate());
        acVoucherResponse.setPostFlag(acVoucher.isPostFlag());
        if (acVoucher.getCollection() != null) {
            acVoucherResponse.setCollection(acCollectionService.getAcCollectionResponse(acVoucher.getCollection()));
        }
        if (acVoucher.getPostedBy() != null) {
            acVoucherResponse.setPostedBy(playerService.getPlayerResponse(acVoucher.getPostedBy()));
        }

        if (Boolean.TRUE.equals(isDetailsResponse)) {
            acVoucherResponse.setDetails(
                    acVoucher.getDetails().stream().map(details -> AcVoucherDetailResponse.builder()
                            .cr(details.getCr())
                            .dr(details.getDr())
                            .acChart(
                                    accountChartService.getAcChartResponse(details.getAcChart())
                            )
                            .narration(details.getNarration())
                            .referenceNo(details.getReferenceNo())
                            .id(details.getId())
                            .build()).toList());
        }

        return acVoucherResponse;
    }

    @Transactional
    public Long saveVoucher(@Valid AcVoucherRequest request) {

        AcVoucherType voucherType = acVoucherTypeService.
                getAcVoucherTypeById(request.getVoucherTypeId());

        // Calculate the sum of Cr values in BigDecimal
        BigDecimal crSum = request.getDetails().stream()
                .map(AcVoucherDetailRequest::getCr)  // Extract the Cr value
                .filter(Objects::nonNull)     // Filter out null Cr values
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate the sum of Dr values in BigDecimal
        BigDecimal drSum = request.getDetails().stream()
                .map(AcVoucherDetailRequest::getDr)  // Extract the Cr value
                .filter(Objects::nonNull)     // Filter out null Cr values
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!drSum.equals(crSum)) {
            throw new BadrRequestException(AC_VOUCHER_DR_CR_AMOUNT_NOT_SAME, HttpStatus.BAD_REQUEST);
        }

        AcVoucher acVoucher = new AcVoucher();
        acVoucher.setVoucherDate(request.getVoucherDate());
        acVoucher.setVoucherType(voucherType);
        acVoucher.setCode(generateUniqueCode(voucherType));
        acVoucher.setNarration(request.getNarration());
        acVoucher.setAmount(crSum);
        acVoucher.setCollection(request.getCollection());

        if (request.isPostFlag()) {
//            if (!ObjectUtils.isEmpty(getLoggedInPlayer())) {
//                acVoucher.setPostedBy(getLoggedInPlayer());
                acVoucher.setPostDate(LocalDate.now());
                acVoucher.setPostFlag(request.isPostFlag());
//            }
        }

        List<AcVoucherDetail> detailsEntity = request.getDetails().stream().map(detailRequest -> {
            AcChart acChart = acChartService.getAcChartById(detailRequest.getAcChartId());
            AcVoucherDetail acVoucherDetail = new AcVoucherDetail();
            acVoucherDetail.setCr(detailRequest.getCr());
            acVoucherDetail.setDr(detailRequest.getDr());
            acVoucherDetail.setNarration(detailRequest.getNarration());
            acVoucherDetail.setAcChart(acChart);
            acVoucherDetail.setReferenceNo(detailRequest.getReferenceNo());
            acVoucherDetail.setVoucher(acVoucher);
            return acVoucherDetail;
        }).toList();

        acVoucher.setDetails(detailsEntity);


        return acVoucherRepository.save(acVoucher).getId();
    }

    private String generateUniqueCode(AcVoucherType voucherType) {
        String code;
        do {
            code = voucherType.getAlias() + Year.now() + RandomUtil.generateRandomString(4);
        } while (isVoucherCodeExists(code));
        return code;
    }

    private boolean isVoucherCodeExists(String code) {
        return acVoucherRepository.findByCode(code) != null;
    }


}
