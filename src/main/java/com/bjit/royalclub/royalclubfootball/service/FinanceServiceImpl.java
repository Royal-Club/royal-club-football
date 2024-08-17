package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.CostType;
import com.bjit.royalclub.royalclubfootball.entity.MonthlyCollection;
import com.bjit.royalclub.royalclubfootball.entity.MonthlyCost;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.CostTypeServiceException;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.MonthlyCostRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentResponse;
import com.bjit.royalclub.royalclubfootball.repository.CostTypeRepository;
import com.bjit.royalclub.royalclubfootball.repository.MonthlyCollectionRepository;
import com.bjit.royalclub.royalclubfootball.repository.MonthlyCostRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.COST_TYPE_IS_NOT_FOUND;
import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {
    private final PlayerRepository playerRepository;
    private final MonthlyCollectionRepository monthlyCollectionRepository;
    private final CostTypeRepository costTypeRepository;
    private final MonthlyCostRepository monthlyCostRepository;

    @Override
    public PaymentResponse paymentCollection(PaymentCollectionRequest paymentRequest) {

        Player player = playerRepository.findById(paymentRequest.getPlayerId())
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        String finalDescription = paymentRequest.getDescription();
        if (paymentRequest.getMonthOfPayment().isBefore(LocalDate.now().withDayOfMonth(1))) {
            finalDescription += " (Late Payment)";
        }

        MonthlyCollection collection = MonthlyCollection.builder()
                .player(player)
                .amount(paymentRequest.getAmount())
                .monthOfPayment(paymentRequest.getMonthOfPayment())
                .description(finalDescription)
                .isPaid(true)
                .createdDate(LocalDateTime.now())
                .build();

        MonthlyCollection savedCollection = monthlyCollectionRepository.save(collection);

        return convertToPaymentResponse(savedCollection);
    }


    @Override
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
                .createdDate(LocalDateTime.now())
                .build();
    }

    private PaymentResponse convertToPaymentResponse(MonthlyCollection collection) {
        return PaymentResponse.builder()
                .playerId(collection.getPlayer().getId())
                .playerName(collection.getPlayer().getName())
                .paymentMonth(collection.getMonthOfPayment())
                .amount(collection.getAmount())
                .build();
    }
}
