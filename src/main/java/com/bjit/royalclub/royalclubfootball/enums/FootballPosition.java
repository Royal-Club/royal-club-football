package com.bjit.royalclub.royalclubfootball.enums;

public enum FootballPosition {
    UNASSIGNED("Unassigned"),
    GOALKEEPER("Goalkeeper"),
    RIGHT_BACK("Right Back"),
    LEFT_BACK("Left Back"),
    CENTER_BACK_1("Center Back"),
    CENTER_BACK_2("Center Back"),
    DEFENSIVE_MIDFIELD("Defensive Midfield"),
    RIGHT_WING_FORWARD("Right Wing/Forward"),
    CENTRAL_MIDFIELD("Central Midfield"),
    STRIKER("Striker"),
    ATTACKING_MIDFIELD("Attacking Midfield"),
    LEFT_WING_FORWARD("Left Wing/Forward");

    private final String description;

    FootballPosition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
