package com.bjit.royalclub.royalclubfootball.model.account.report;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PlayerCollectionReport {
    private Long playerId;
    private String playerName;
    private boolean isActive;
    private Map<Integer, Map<Integer, BigDecimal>> yearMonthAmount;
}