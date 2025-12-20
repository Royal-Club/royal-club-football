package com.bjit.royalclub.royalclubfootball.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
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

    private static final Map<String, FootballPosition> STRING_TO_ENUM = new HashMap<>();

    static {
        for (FootballPosition position : values()) {
            STRING_TO_ENUM.put(position.description.toUpperCase(), position);
        }
    }

    public static FootballPosition getPositionOrDefault(String position) {
        if (position == null) {
            return UNASSIGNED;
        }
        FootballPosition result = STRING_TO_ENUM.get(position.toUpperCase());
        return result != null ? result : UNASSIGNED;
    }
}
