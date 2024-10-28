package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.CostType;
import com.bjit.royalclub.royalclubfootball.entity.account.AcBillPayment;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.account.AcBillPaymentRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcBillPaymentResponse;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherDetailRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherRequest;
import com.bjit.royalclub.royalclubfootball.repository.account.AcBillPaymentRepository;
import com.bjit.royalclub.royalclubfootball.service.CostTypeService;
import com.bjit.royalclub.royalclubfootball.util.RandomUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_BILL_PAYMENT_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service
public class AcBillPaymentService {

    private final AcBillPaymentRepository repository;
    private final CostTypeService costTypeService;
    private final AcVoucherService voucherService;

    /**
     * Retrieves all bill payments.
     */
    public List<AcBillPaymentResponse> getAllBillPayments() {
        List<AcBillPayment> billPayments = repository.findAll();
        return billPayments.stream().map(this::convertBillPaymentDto).toList();
    }

    public Page<AcBillPaymentResponse> getPaginatedAcBillPayments(int page, int size, String sortBy, String order, Integer year, Integer month) {
        Sort sort = Sort.by(Sort.Direction.fromString(order == null ? "ASC" : order), sortBy == null ? "paymentDate" : sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AcBillPayment> paymentPage;
        if (year != null && month != null) {
            paymentPage = repository.findByYearAndMonth(year, month, pageable);
        } else if (year != null) {
            paymentPage = repository.findByYear(year, pageable);
        } else {
            paymentPage = repository.findAll(pageable);
        }

        return paymentPage.map(this::convertBillPaymentDto);
    }

    /**
     * Saves a new bill payment.
     */
    @Transactional
    public Long save(AcBillPaymentRequest request) {
        AcBillPayment billPayment = createOrUpdateBillPaymentEntity(null, request);
        billPayment = repository.save(billPayment);

        handleVoucherForBillPayment(billPayment, request, null);

        return billPayment.getId();
    }

    /**
     * Updates an existing bill payment and its associated voucher.
     */
    @Transactional
    public Long update(Long id, AcBillPaymentRequest request) {
        AcBillPayment existingBillPayment = createOrUpdateBillPaymentEntity(id, request);
        repository.save(existingBillPayment);

        handleVoucherForBillPayment(existingBillPayment, request, existingBillPayment.getVoucher() != null ? existingBillPayment.getVoucher().getId() : null);

        return existingBillPayment.getId();
    }

    /**
     * Deletes a bill payment by ID.
     */
    @Transactional
    public void delete(Long id) {
        AcBillPayment existingBillPayment = getAcBillPaymentEntity(id);
        repository.delete(existingBillPayment);
    }

    /**
     * Handles saving or updating the voucher for a bill payment, based on whether a voucher ID is provided.
     */
    private void handleVoucherForBillPayment(AcBillPayment billPayment, AcBillPaymentRequest request, Long voucherId) {
        AcVoucherRequest voucherRequest = new AcVoucherRequest();
        voucherRequest.setBillPayment(billPayment);
        voucherRequest.setVoucherDate(billPayment.getPaymentDate());
        voucherRequest.setVoucherTypeId(1L);
        voucherRequest.setNarration(request.getDescription());
        voucherRequest.setPostFlag(true);

        List<AcVoucherDetailRequest> voucherDetailRequests = new ArrayList<>();

        AcVoucherDetailRequest drDetail = AcVoucherDetailRequest.builder()
                .dr(request.getAmount())
                .acChartId(billPayment.getCostType().getChart().getId())
                .build();

        AcVoucherDetailRequest crDetail = AcVoucherDetailRequest.builder()
                .cr(request.getAmount())
                .acChartId(4L)
                .build();

        voucherDetailRequests.add(drDetail);
        voucherDetailRequests.add(crDetail);

        voucherRequest.setDetails(voucherDetailRequests);

        if (voucherId != null) {
            voucherService.updateVoucher(voucherId, voucherRequest);
        } else {
            voucherService.saveVoucher(voucherRequest);
        }
    }

    /**
     * Creates or updates the bill payment entity based on the input request.
     */
    private AcBillPayment createOrUpdateBillPaymentEntity(Long id, AcBillPaymentRequest request) {
        AcBillPayment billPayment = (id != null) ? getAcBillPaymentEntity(id) : new AcBillPayment();

        CostType costType = costTypeService.getCostTypeEntity(request.getCostTypeId());

        billPayment.setAmount(request.getAmount());
        billPayment.setPaymentDate(request.getPaymentDate());
        billPayment.setDescription(request.getDescription());
        billPayment.setCostType(costType);

        if (id == null) {
            billPayment.setCode(generateUniqueBillCode());
        }

        return billPayment;
    }

    /**
     * Retrieves a bill payment by ID.
     */
    public AcBillPayment getAcBillPaymentEntity(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(AC_BILL_PAYMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    /**
     * Generates a unique bill code.
     */
    private String generateUniqueBillCode() {
        String code;
        do {
            code = "BIL" + RandomUtil.generateRandomString(10);
        } while (isBillCodeExists(code));
        return code;
    }

    private boolean isBillCodeExists(String code) {
        return repository.existsByCode(code);
    }

    /**
     * Converts the bill payment entity to response DTO.
     */
    public AcBillPaymentResponse getAcBillPaymentResponse(AcBillPayment billPayment) {
        return convertBillPaymentDto(billPayment);
    }

    private AcBillPaymentResponse convertBillPaymentDto(AcBillPayment billPayment) {
        AcBillPaymentResponse response = new AcBillPaymentResponse();

        response.setId(billPayment.getId());
        response.setCode(billPayment.getCode());
        response.setPaymentDate(billPayment.getPaymentDate());
        response.setAmount(billPayment.getAmount());
        response.setDescription(billPayment.getDescription());
        response.setCostType(costTypeService.getByCostType(billPayment.getCostType()));

        if (billPayment.getVoucher() != null) {
            response.setVoucherId(billPayment.getVoucher().getId());
            response.setVoucherCode(billPayment.getVoucher().getCode());
        }

        return response;
    }
}
