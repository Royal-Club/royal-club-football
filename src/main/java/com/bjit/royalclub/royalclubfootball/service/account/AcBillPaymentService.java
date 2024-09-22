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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    public List<AcBillPaymentResponse> getAllBillPayments() {
        List<AcBillPayment> billPayments = repository.findAll();
        return billPayments.stream().map(this::getAcBillPaymentResponse).toList();
    }

    @Transactional
    public Long save(AcBillPaymentRequest request) {

        CostType costType = costTypeService.getCostTypeEntity(request.getCostTypeId());

        AcBillPayment billPayment = new AcBillPayment();
        billPayment.setAmount(request.getAmount());
        billPayment.setPaymentDate(request.getPaymentDate());
        billPayment.setPaid(request.isPaid());
        billPayment.setCode(generateUniqueBillCode());
        billPayment.setDescription(request.getDescription());
        billPayment.setCostType(costType);


        billPayment = repository.save(billPayment);

        // Voucher entry
        AcVoucherRequest voucherRequest = new AcVoucherRequest();
        voucherRequest.setBillPayment(billPayment);
        voucherRequest.setVoucherDate(LocalDate.now());
        voucherRequest.setVoucherTypeId(1L);
        voucherRequest.setNarration(request.getDescription());
        voucherRequest.setPostFlag(true);

        List<AcVoucherDetailRequest> voucherDetailRequests = new ArrayList<>();

        AcVoucherDetailRequest drDetail = AcVoucherDetailRequest.builder()
                .dr(request.getAmount())
//                .referenceNo("Payment")
                .acChartId(costType.getChart().getId())
                .build();

        AcVoucherDetailRequest crDetail = AcVoucherDetailRequest.builder()
                .cr(request.getAmount())
//                .referenceNo("Payment")
                .acChartId(4L)
                .build();

        voucherDetailRequests.add(drDetail);
        voucherDetailRequests.add(crDetail);

        voucherRequest.setDetails(voucherDetailRequests);

        voucherService.saveVoucher(voucherRequest);

        return billPayment.getId();
    }

    public AcBillPayment getAcBillPaymentEntity(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(AC_BILL_PAYMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

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


    public AcBillPaymentResponse getAcBillPaymentResponse(Long id) {
        return convertBillPaymentDto(getAcBillPaymentEntity(id));
    }

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
        response.setPaid(billPayment.isPaid());
        response.setCostType(costTypeService.getByCostType(billPayment.getCostType()));

        if (billPayment.getVoucher() != null) {
            response.setVoucherId(billPayment.getVoucher().getId());
            response.setVoucherCode(billPayment.getVoucher().getCode());
        }

        return response;
    }
}
