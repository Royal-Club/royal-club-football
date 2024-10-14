package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.account.AcChart;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucher;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherDetail;
import com.bjit.royalclub.royalclubfootball.entity.account.AcVoucherType;
import com.bjit.royalclub.royalclubfootball.exception.BadRequestException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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

    @Autowired
    @Lazy
    private AcBillPaymentService acBillPaymentService;

    /**
     * Retrieves all vouchers, with optional detailed response.
     */
    public List<AcVoucherResponse> getAllAcVouchers(Boolean isDetailsResponse) {
        return acVoucherRepository.findAll()
                .stream()
                .map(entity -> getAcVoucherResponse(entity.getId(), isDetailsResponse))
                .toList();
    }

    /**
     * Retrieves a specific voucher by ID with optional detailed response.
     */
    public AcVoucherResponse getAcVoucherResponse(Long id, Boolean isDetailsResponse) {
        return getAcVoucherResponse(getAcVoucher(id), isDetailsResponse);
    }

    /**
     * Retrieves a voucher by ID or throws ResourceNotFoundException if not found.
     */
    public AcVoucher getAcVoucher(Long id) {
        return acVoucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AC_VOUCHER_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    /**
     * Converts a voucher entity to its DTO with optional detailed response.
     */
    public AcVoucherResponse getAcVoucherResponse(AcVoucher acVoucher, Boolean isDetailsResponse) {
        AcVoucherResponse response = new AcVoucherResponse();
        mapVoucherBasicFields(acVoucher, response);
        mapRelatedEntities(acVoucher, response);

        if (Boolean.TRUE.equals(isDetailsResponse)) {
            response.setDetails(
                    acVoucher.getDetails().stream()
                            .map(this::convertDetailToResponse)
                            .toList()
            );
        }
        return response;
    }

    /**
     * Maps basic fields from the voucher entity to the response DTO.
     */
    private void mapVoucherBasicFields(AcVoucher acVoucher, AcVoucherResponse response) {
        response.setId(acVoucher.getId());
        response.setAmount(acVoucher.getAmount());
        response.setNarration(acVoucher.getNarration());
        response.setCode(acVoucher.getCode());
        response.setVoucherDate(acVoucher.getVoucherDate());
        response.setVoucherType(acVoucher.getVoucherType());
        response.setPostDate(acVoucher.getPostDate());
        response.setPostFlag(acVoucher.isPostFlag());
    }

    /**
     * Maps related entities like collection, player, and bill payment to the response DTO.
     */
    private void mapRelatedEntities(AcVoucher acVoucher, AcVoucherResponse response) {
        if (acVoucher.getCollection() != null) {
            response.setCollection(acCollectionService.getAcCollectionResponse(acVoucher.getCollection()));
        }

        if (acVoucher.getPostedBy() != null) {
            response.setPostedBy(playerService.getPlayerResponse(acVoucher.getPostedBy()));
        }

        if (acVoucher.getBillPayment() != null) {
            response.setBillPayment(acBillPaymentService.getAcBillPaymentResponse(acVoucher.getBillPayment()));
        }
    }

    /**
     * Converts a voucher detail entity to its response DTO.
     */
    private AcVoucherDetailResponse convertDetailToResponse(AcVoucherDetail details) {
        return AcVoucherDetailResponse.builder()
                .cr(details.getCr())
                .dr(details.getDr())
                .acChart(accountChartService.getAcChartResponse(details.getAcChart()))
                .narration(details.getNarration())
                .referenceNo(details.getReferenceNo())
                .id(details.getId())
                .build();
    }

    /**
     * Saves a new voucher and its details, ensuring Cr and Dr sums match.
     */
    @Transactional
    public Long saveVoucher(@Valid AcVoucherRequest request) {
        BigDecimal crSum = calculateSum(request.getDetails(), AcVoucherDetailRequest::getCr);
        BigDecimal drSum = calculateSum(request.getDetails(), AcVoucherDetailRequest::getDr);

        if (!drSum.equals(crSum)) {
            throw new BadRequestException(AC_VOUCHER_DR_CR_AMOUNT_NOT_SAME, HttpStatus.BAD_REQUEST);
        }

        AcVoucher acVoucher = buildAcVoucherFromRequest(request, crSum);
        acVoucher.setDetails(buildVoucherDetails(request, acVoucher));

        return acVoucherRepository.save(acVoucher).getId();
    }

    /**
     * Updates an existing voucher.
     *
     * @param id      The ID of the voucher to update.
     * @param request The new voucher request data.
     * @return The updated voucher ID.
     */
    @Transactional
    public Long updateVoucher(Long id, @Valid AcVoucherRequest request) {
        AcVoucher existingVoucher = getAcVoucher(id);

        BigDecimal crSum = calculateSum(request.getDetails(), AcVoucherDetailRequest::getCr);
        BigDecimal drSum = calculateSum(request.getDetails(), AcVoucherDetailRequest::getDr);

        if (!drSum.equals(crSum)) {
            throw new BadRequestException(AC_VOUCHER_DR_CR_AMOUNT_NOT_SAME, HttpStatus.BAD_REQUEST);
        }

        existingVoucher.setVoucherDate(request.getVoucherDate());
        existingVoucher.setNarration(request.getNarration());
        existingVoucher.setAmount(crSum);
        existingVoucher.setCollection(request.getCollection());
        existingVoucher.setBillPayment(request.getBillPayment());

        // Updating post flag and post date if necessary
        if (request.isPostFlag() && !existingVoucher.isPostFlag()) {
            existingVoucher.setPostedBy(getLoggedInPlayer());
            existingVoucher.setPostDate(LocalDate.now());
            existingVoucher.setPostFlag(true);
        }

        // Update voucher details - clear and add new details
        existingVoucher.getDetails().clear();
        existingVoucher.getDetails().addAll(buildVoucherDetails(request, existingVoucher));

        return acVoucherRepository.save(existingVoucher).getId();
    }


    /**
     * Deletes an existing voucher by ID.
     *
     * @param id The ID of the voucher to delete.
     */
    @Transactional
    public void deleteVoucher(Long id) {
        AcVoucher existingVoucher = getAcVoucher(id); // Fetch the voucher to delete

        acVoucherRepository.delete(existingVoucher); // Perform the delete operation
    }

    // Utility methods...

    /**
     * Calculates the sum of Dr or Cr values from the voucher details.
     */
    private BigDecimal calculateSum(List<AcVoucherDetailRequest> details, Function<AcVoucherDetailRequest, BigDecimal> getter) {
        return details.stream()
                .map(getter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Builds the voucher details from the request.
     */
    private List<AcVoucherDetail> buildVoucherDetails(AcVoucherRequest request, AcVoucher acVoucher) {
        return request.getDetails().stream()
                .map(detailRequest -> {
                    AcChart acChart = acChartService.getAcChartById(detailRequest.getAcChartId());
                    AcVoucherDetail acVoucherDetail = new AcVoucherDetail();
                    acVoucherDetail.setCr(detailRequest.getCr());
                    acVoucherDetail.setDr(detailRequest.getDr());
                    acVoucherDetail.setNarration(detailRequest.getNarration());
                    acVoucherDetail.setAcChart(acChart);
                    acVoucherDetail.setReferenceNo(detailRequest.getReferenceNo());
                    acVoucherDetail.setVoucher(acVoucher);
                    return acVoucherDetail;
                })
                .toList();
    }

    /**
     * Builds a new AcVoucher entity from the request.
     */
    private AcVoucher buildAcVoucherFromRequest(AcVoucherRequest request, BigDecimal amount) {
        AcVoucherType voucherType = acVoucherTypeService.getAcVoucherTypeById(request.getVoucherTypeId());
        AcVoucher acVoucher = new AcVoucher();
        acVoucher.setVoucherDate(request.getVoucherDate());
        acVoucher.setVoucherType(voucherType);
        acVoucher.setCode(generateUniqueCode(voucherType));
        acVoucher.setNarration(request.getNarration());
        acVoucher.setAmount(amount);
        acVoucher.setCollection(request.getCollection());
        acVoucher.setBillPayment(request.getBillPayment());

        if (request.isPostFlag()) {
            acVoucher.setPostedBy(getLoggedInPlayer());
            acVoucher.setPostDate(LocalDate.now());
            acVoucher.setPostFlag(request.isPostFlag());
        }

        return acVoucher;
    }

    /**
     * Generates a unique voucher code.
     */
    private String generateUniqueCode(AcVoucherType voucherType) {
        String code;
        do {
            code = voucherType.getAlias() + Year.now() + RandomUtil.generateRandomString(4);
        } while (isVoucherCodeExists(code));
        return code;
    }

    /**
     * Checks if a voucher code already exists.
     */
    private boolean isVoucherCodeExists(String code) {
        return acVoucherRepository.findByCode(code) != null;
    }
}
