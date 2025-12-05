package com.bjit.royalclub.royalclubfootball.enums;

public enum RoundFormat {
    ROUND_ROBIN,           // League style
    SINGLE_ELIMINATION,    // Knockout (lose = out)
    DOUBLE_ELIMINATION,    // Knockout with losers bracket
    SWISS_SYSTEM,          // Pairings based on standings
    CUSTOM                 // Manual control
}
