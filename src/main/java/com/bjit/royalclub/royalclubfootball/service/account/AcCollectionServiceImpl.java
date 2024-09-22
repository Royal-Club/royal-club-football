package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.CostType;
import com.bjit.royalclub.royalclubfootball.entity.account.AcCollection;
import com.bjit.royalclub.royalclubfootball.entity.MonthlyCost;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.CostTypeServiceException;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcCollectionResponse;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherDetailRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherRequest;
import com.bjit.royalclub.royalclubfootball.repository.CostTypeRepository;
import com.bjit.royalclub.royalclubfootball.repository.account.AcCollectionRepository;
import com.bjit.royalclub.royalclubfootball.repository.MonthlyCostRepository;
import com.bjit.royalclub.royalclubfootball.service.PlayerService;
import com.bjit.royalclub.royalclubfootball.util.RandomUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.AC_COLLECTION_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.COST_TYPE_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AcCollectionServiceImpl implements AcCollectionService {

    private final PlayerService playerService;
    private final AcCollectionRepository repository;
    private final CostTypeRepository costTypeRepository;
    private final MonthlyCostRepository monthlyCostRepository;
    @Lazy
    @Autowired
    private AcVoucherService acVoucherService;

    @Transactional
    @Override
    public Long paymentCollection(PaymentCollectionRequest paymentRequest) {

        Set<Player> players = new HashSet<>();
        paymentRequest.getPlayerIds().forEach(id ->
                players.add(playerService.getPlayerEntity(id)));

//        LocalDate paymentMonth = LocalDate.MIN

//        String finalDescription = paymentRequest.getDescription();
//        if (paymentRequest.getMonthOfPayment().isBefore(LocalDate.now().withDayOfMonth(5))) {
//            finalDescription += " (Late Payment)";
//        }

        AcCollection collection = AcCollection.builder()
                .transactionId(generateUniqueTransactionId())
                .players(players)
                .amount(paymentRequest.getAmount())
                .totalAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(players.size())))
                .monthOfPayment(paymentRequest.getMonthOfPayment())
                .description(paymentRequest.getDescription())
                .isPaid(true)
                .build();

        collection = repository.save(collection);

        AcVoucherRequest voucherRequest = new AcVoucherRequest();
        voucherRequest.setCollection(collection);
        voucherRequest.setVoucherDate(LocalDate.now());
        voucherRequest.setVoucherTypeId(2L);
        voucherRequest.setNarration(paymentRequest.getDescription());
        voucherRequest.setPostFlag(true);

        List<AcVoucherDetailRequest> voucherDetailRequests = new ArrayList<>();

        AcVoucherDetailRequest drDetail = AcVoucherDetailRequest.builder()
                .dr(paymentRequest.getAmount().multiply(BigDecimal.valueOf(players.size())))
                .referenceNo("Monthly collection.")
                .acChartId(4L)
                .build();

        AcVoucherDetailRequest crDetail = AcVoucherDetailRequest.builder()
                .cr(paymentRequest.getAmount().multiply(BigDecimal.valueOf(players.size())))
                .referenceNo("Monthly collection.")
                .acChartId(10L)
                .build();

        voucherDetailRequests.add(drDetail);
        voucherDetailRequests.add(crDetail);

        voucherRequest.setDetails(voucherDetailRequests);

        acVoucherService.saveVoucher(voucherRequest);

        return collection.getId();
    }

    @Override
    public List<AcCollectionResponse> getAllAcCollections() {
        List<AcCollection> entities = repository.findAll();

        return entities.stream().map(this::convertToPaymentResponse).toList();
    }

    private String generateUniqueTransactionId() {
        String transactionId;
        do {
            transactionId = "COL" + RandomUtil.generateRandomString(10);
        } while (isTransactionIdExists(transactionId));
        return transactionId;
    }

    private boolean isTransactionIdExists(String transactionId) {
        return repository.findByTransactionId(transactionId) != null;
    }


    @Override
    public void recordCost(MonthlyCostRequest costRequest) {
        CostType costType = costTypeRepository.findById(costRequest.getCostTypeId())
                .orElseThrow(() -> new CostTypeServiceException(COST_TYPE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        MonthlyCost cost = convertToEntity(costRequest);
        cost.setCostType(costType);
        monthlyCostRepository.save(cost);
    }

//
//    private double fetchTotalCollection(LocalDate monthStart, LocalDate monthEnd) {
//        return repository.findByMonthOfPaymentBetween(monthStart, monthEnd)
//                .stream()
//                .mapToDouble(AcCollection::getAmount)
//                .sum();
//    }

    private double fetchTotalCost(LocalDate monthStart, LocalDate monthEnd) {
        return monthlyCostRepository.findByMonthOfCostBetween(monthStart, monthEnd)
                .stream()
                .mapToDouble(MonthlyCost::getAmount)
                .sum();
    }

    private Map<String, Double> fetchCostTypeWiseCost(LocalDate monthStart, LocalDate monthEnd) {
        return monthlyCostRepository.findByMonthOfCostBetween(monthStart, monthEnd)
                .stream()
                .collect(Collectors.groupingBy(cost -> cost.getCostType().getName(),
                        Collectors.summingDouble(MonthlyCost::getAmount)));
    }

//    private double fetchCashOnPreviousMonth(LocalDate date) {
//        // Calculate the first and last day of the previous month
//        LocalDate startOfPreviousMonth = date.minusMonths(1).withDayOfMonth(1);
//        LocalDate endOfPreviousMonth = startOfPreviousMonth.withDayOfMonth(startOfPreviousMonth.lengthOfMonth());
//
//        double totalCollection = fetchTotalCollection(startOfPreviousMonth, endOfPreviousMonth);
//        double totalCost = fetchTotalCost(startOfPreviousMonth, endOfPreviousMonth);
//        return totalCollection - totalCost;
//    }

    private MonthlyCost convertToEntity(MonthlyCostRequest costRequest) {
        return MonthlyCost.builder()
                .monthOfCost(costRequest.getMonthOfCost())
                .amount(costRequest.getAmount())
                .description(costRequest.getDescription())
                .build();
    }

    public AcCollection getAcCollectionById(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(AC_COLLECTION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    public AcCollectionResponse getAcCollectionResponse(AcCollection acCollection) {
        return convertToPaymentResponse(acCollection);
    }

    private AcCollectionResponse convertToPaymentResponse(AcCollection collection) {
        String allPayersName = collection.getPlayers().stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));

        AcCollectionResponse acCollectionResponse = AcCollectionResponse.builder()
                .id(collection.getId())
                .transactionId(collection.getTransactionId())
                .monthOfPayment(collection.getMonthOfPayment())
                .amount(collection.getAmount())
                .totalAmount(collection.getTotalAmount())
                .isPaid(collection.isPaid())
                .players(playerService.getPlayerResponses(collection.getPlayers()))
                .createdDate(collection.getCreatedDate())
                .updatedDate(collection.getUpdatedDate())
                .allPayersName(allPayersName)
                .build();
        if (ObjectUtils.isNotEmpty(collection.getVoucher())) {
            acCollectionResponse.setVoucherCode(collection.getVoucher().getCode());
            acCollectionResponse.setVoucherId(collection.getVoucher().getId());
        }
        return acCollectionResponse;
    }
}
