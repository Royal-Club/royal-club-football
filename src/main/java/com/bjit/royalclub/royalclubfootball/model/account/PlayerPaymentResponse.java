package com.bjit.royalclub.royalclubfootball.model.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One line of a player's own payment history. Deliberately leaner than {@link AcCollectionResponse}:
 * a collection can cover many players, but a player viewing their history only needs their own share
 * ({@code amount}), not the batch total or the other payers.
 */
@Data
@Builder
public class PlayerPaymentResponse {
    private Long collectionId;
    private String transactionId;
    /** The month these dues are for. */
    private LocalDate monthOfPayment;
    /** The date the money was actually received. */
    private LocalDate date;
    /** This player's share of the collection. */
    private BigDecimal amount;
    private String description;
}
