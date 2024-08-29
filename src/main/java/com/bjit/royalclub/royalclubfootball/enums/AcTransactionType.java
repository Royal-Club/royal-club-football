package com.bjit.royalclub.royalclubfootball.enums;

public enum AcTransactionType {
    CO("Cash Out"),
    CI("Cash In"),
    JL("Journal");

    private final String description;

    AcTransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}
