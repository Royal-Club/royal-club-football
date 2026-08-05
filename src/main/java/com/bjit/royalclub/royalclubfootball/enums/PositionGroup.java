package com.bjit.royalclub.royalclubfootball.enums;

import lombok.Getter;

/**
 * The band a formation slot sits in. Deliberately coarser than
 * {@link FootballPosition}: that enum is modelled on eleven-a-side and has no
 * sensible member for, say, the wide slot in a futsal 2-2-1. A formation slot
 * only needs to know which line it belongs to — the exact spot comes from the
 * slot's x/y co-ordinates.
 */
@Getter
public enum PositionGroup {
    GK("Goalkeeper"),
    DEF("Defence"),
    MID("Midfield"),
    FWD("Attack");

    private final String description;

    PositionGroup(String description) {
        this.description = description;
    }

    public static PositionGroup getGroupOrDefault(String group) {
        if (group == null || group.trim().isEmpty()) {
            return MID;
        }
        try {
            return PositionGroup.valueOf(group.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MID;
        }
    }
}
