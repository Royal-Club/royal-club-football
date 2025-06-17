package com.bjit.royalclub.royalclubfootball.service.account;

import com.bjit.royalclub.royalclubfootball.entity.CostType;
import com.bjit.royalclub.royalclubfootball.entity.MonthlyCost;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.entity.account.AcCollection;
import com.bjit.royalclub.royalclubfootball.exception.CostTypeServiceException;
import com.bjit.royalclub.royalclubfootball.exception.ResourceNotFoundException;
import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcCollectionResponse;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherDetailRequest;
import com.bjit.royalclub.royalclubfootball.model.account.AcVoucherRequest;
import com.bjit.royalclub.royalclubfootball.model.account.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.account.report.PlayerCollectionMetricsResponse;
import com.bjit.royalclub.royalclubfootball.model.account.report.PlayerCollectionReport;
import com.bjit.royalclub.royalclubfootball.repository.CostTypeRepository;
import com.bjit.royalclub.royalclubfootball.repository.MonthlyCostRepository;
import com.bjit.royalclub.royalclubfootball.repository.account.AcCollectionRepository;
import com.bjit.royalclub.royalclubfootball.service.PlayerService;
import com.bjit.royalclub.royalclubfootball.util.RandomUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * Saves a new payment collection.
     */
    @Transactional
    @Override
    public Long savePaymentCollection(PaymentCollectionRequest paymentRequest) {
        Set<Player> players = paymentRequest.getPlayerIds().stream()
                .map(playerService::getPlayerEntity)
                .collect(Collectors.toSet());

        AcCollection collection = AcCollection.builder()
                .transactionId(generateUniqueTransactionId())
                .players(players)
                .amount(paymentRequest.getAmount())
                .totalAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(players.size())))
                .monthOfPayment(paymentRequest.getMonthOfPayment())
                .description(paymentRequest.getDescription())
                .date(paymentRequest.getDate()) // Set the date from the request
                .build();

        collection = repository.save(collection);

        handleVoucherForCollection(paymentRequest, collection, players, null);

        return collection.getId();
    }

    /**
     * Updates an existing payment collection.
     */
    @Transactional
    @Override
    public Long updatePaymentCollection(Long id, PaymentCollectionRequest paymentRequest) {
        AcCollection existingCollection = getAcCollectionById(id);

        Set<Player> players = paymentRequest.getPlayerIds().stream()
                .map(playerService::getPlayerEntity)
                .collect(Collectors.toSet());

        existingCollection.setPlayers(players);
        existingCollection.setAmount(paymentRequest.getAmount());
        existingCollection.setTotalAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(players.size())));
        existingCollection.setMonthOfPayment(paymentRequest.getMonthOfPayment());
        existingCollection.setDescription(paymentRequest.getDescription());
        existingCollection.setDate(paymentRequest.getDate()); // Set the updated date

        repository.save(existingCollection);

        handleVoucherForCollection(paymentRequest, existingCollection, players, existingCollection.getVoucher() != null ? existingCollection.getVoucher().getId() : null);

        return existingCollection.getId();
    }

    /**
     * Deletes a payment collection by ID.
     */
    @Transactional
    @Override
    public void deletePaymentCollection(Long id) {
        AcCollection existingCollection = getAcCollectionById(id);
        repository.delete(existingCollection);
    }

    /**
     * Retrieves all payment collections.
     */
    @Override
    public List<AcCollectionResponse> getAllAcCollections() {
        List<AcCollection> entities = repository.findAll();
        return entities.stream().map(this::convertToPaymentResponse).toList();
    }

    /**
     * Records a cost for a given month.
     */
    @Override
    @Transactional
    public void recordCost(MonthlyCostRequest costRequest) {
        CostType costType = costTypeRepository.findById(costRequest.getCostTypeId())
                .orElseThrow(() -> new CostTypeServiceException(COST_TYPE_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        MonthlyCost cost = convertToEntity(costRequest);
        cost.setCostType(costType);
        monthlyCostRepository.save(cost);
    }

    private MonthlyCost convertToEntity(MonthlyCostRequest costRequest) {
        return MonthlyCost.builder()
                .monthOfCost(costRequest.getMonthOfCost())
                .amount(costRequest.getAmount())
                .description(costRequest.getDescription())
                .build();
    }

    /**
     * Generates a unique transaction ID.
     */
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

    /**
     * Fetches a payment collection by ID or throws ResourceNotFoundException if not found.
     */
    public AcCollection getAcCollectionById(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(AC_COLLECTION_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    /**
     * Converts an AcCollection entity to an AcCollectionResponse.
     */
    public AcCollectionResponse getAcCollectionResponse(AcCollection acCollection) {
        return convertToPaymentResponse(acCollection);
    }

    private AcCollectionResponse convertToPaymentResponse(AcCollection collection) {
        String allPayersName = collection.getPlayers().stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));

        Set<Long> playerIds = collection.getPlayers().stream()
                .map(Player::getId).collect(Collectors.toSet());

        AcCollectionResponse response = AcCollectionResponse.builder()
                .id(collection.getId())
                .transactionId(collection.getTransactionId())
                .date(collection.getDate())
                .monthOfPayment(collection.getMonthOfPayment())
                .amount(collection.getAmount())
                .totalAmount(collection.getTotalAmount())
                .players(playerService.getPlayerResponses(collection.getPlayers()))
                .allPayersName(allPayersName)
                .playerIds(playerIds)
                .createdDate(collection.getCreatedDate())
                .updatedDate(collection.getUpdatedDate())
                .build();

        if (collection.getVoucher() != null) {
            response.setVoucherCode(collection.getVoucher().getCode());
            response.setVoucherId(collection.getVoucher().getId());
        }

        return response;
    }

    /**
     * Handles saving or updating the voucher for a collection, based on whether a voucher ID is provided.
     */
    private void handleVoucherForCollection(PaymentCollectionRequest paymentRequest, AcCollection collection, Set<Player> players, Long voucherId) {
        AcVoucherRequest voucherRequest = new AcVoucherRequest();
        voucherRequest.setCollection(collection);
        voucherRequest.setVoucherDate(paymentRequest.getDate()); // Set the date in the voucher
        voucherRequest.setVoucherTypeId(2L);  // Assuming voucher type ID remains constant
        voucherRequest.setNarration(paymentRequest.getDescription());
        voucherRequest.setPostFlag(true);

        List<AcVoucherDetailRequest> voucherDetailRequests = new ArrayList<>();
        BigDecimal totalAmount = paymentRequest.getAmount().multiply(BigDecimal.valueOf(players.size()));

        AcVoucherDetailRequest drDetail = AcVoucherDetailRequest.builder()
                .dr(totalAmount)
                .referenceNo("Monthly collection.")
                .acChartId(4L)
                .build();

        AcVoucherDetailRequest crDetail = AcVoucherDetailRequest.builder()
                .cr(totalAmount)
                .referenceNo("Monthly collection.")
                .acChartId(10L)
                .build();

        voucherDetailRequests.add(drDetail);
        voucherDetailRequests.add(crDetail);

        voucherRequest.setDetails(voucherDetailRequests);

        if (voucherId != null) {
            // Update existing voucher
            acVoucherService.updateVoucher(voucherId, voucherRequest);
        } else {
            // Save new voucher
            acVoucherService.saveVoucher(voucherRequest);
        }
    }

    /**
     * Retrieves a specific payment collection by ID.
     */
    @Override
    public AcCollectionResponse getAcCollection(Long id) {
        AcCollection collection = getAcCollectionById(id);
        return convertToPaymentResponse(collection);
    }

    // In AcCollectionServiceImpl.java
    @Override
    public PlayerCollectionMetricsResponse getPlayerCollectionMetrics() {
        List<AcCollection> collections = repository.findAll();
        Map<Long, PlayerCollectionReport> reportMap = new HashMap<>();

        for (AcCollection collection : collections) {
            for (Player player : collection.getPlayers()) {
                PlayerCollectionReport report = reportMap.computeIfAbsent(player.getId(), id -> {
                    PlayerCollectionReport r = new PlayerCollectionReport();
                    r.setPlayerId(player.getId());
                    r.setPlayerName(player.getName());
                    r.setYearMonthAmount(new HashMap<>());
                    return r;
                });

                int year = collection.getMonthOfPayment().getYear();
                int month = collection.getMonthOfPayment().getMonthValue();
                report.getYearMonthAmount()
                        .computeIfAbsent(year, y -> new HashMap<>())
                        .merge(month, collection.getAmount(), BigDecimal::add);
            }
        }
        return PlayerCollectionMetricsResponse.builder()
                .metrics(new ArrayList<>(reportMap.values()))
                .years(
                        repository.findAllCollectionYears()
                ).build();
    }
}
