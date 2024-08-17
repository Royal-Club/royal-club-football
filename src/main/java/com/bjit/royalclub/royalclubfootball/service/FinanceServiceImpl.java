package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.entity.MonthlyCollection;
import com.bjit.royalclub.royalclubfootball.entity.Player;
import com.bjit.royalclub.royalclubfootball.exception.PlayerServiceException;
import com.bjit.royalclub.royalclubfootball.model.PaymentCollectionRequest;
import com.bjit.royalclub.royalclubfootball.model.PaymentResponse;
import com.bjit.royalclub.royalclubfootball.repository.MonthlyCollectionRepository;
import com.bjit.royalclub.royalclubfootball.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.bjit.royalclub.royalclubfootball.constant.RestErrorMessageDetail.PLAYER_IS_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class FinanceServiceImpl implements FinanceService {
    private final PlayerRepository playerRepository;
    private final MonthlyCollectionRepository monthlyCollectionRepository;

    @Override
    public PaymentResponse recordCollection(PaymentCollectionRequest paymentRequest) {

        Player player = playerRepository.findById(paymentRequest.getPlayerId())
                .orElseThrow(() -> new PlayerServiceException(PLAYER_IS_NOT_FOUND, HttpStatus.NOT_FOUND));

        String finalDescription = paymentRequest.getDescription();
        if (paymentRequest.getPaymentMonth().isBefore(LocalDate.now().withDayOfMonth(1))) {
            finalDescription += " (Late Payment)";
        }

        MonthlyCollection collection = MonthlyCollection.builder()
                .player(player)
                .amount(paymentRequest.getAmount())
                .paymentMonth(paymentRequest.getPaymentMonth())
                .description(finalDescription)
                .build();

        MonthlyCollection savedCollection = monthlyCollectionRepository.save(collection);

        return convertToPaymentResponse(savedCollection);
    }

    private PaymentResponse convertToPaymentResponse(MonthlyCollection collection) {
        return PaymentResponse.builder()
                .playerName(collection.getPlayer().getName())
                .paymentMonth(collection.getPaymentMonth())
                .amount(collection.getAmount())
                .build();
    }

}
