package com.bjit.royalclub.royalclubfootball.enums;

import lombok.Getter;

@Getter
public enum AcNatureType {
    ASSET("Asset"),
    LIABILITY("Liability"),
    INCOME("Income"),
    EXPENSE("Expense");

    private final String description;

    AcNatureType(String description) {
        this.description = description;
    }

}
