package com.bjit.royalclub.royalclubfootball.enums;

import lombok.Getter;

@Getter
public enum AcTransactionType {
    CO("Cash Out"),
    CI("Cash In"),
    JL("Journal");

    private final String description;

    AcTransactionType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}
