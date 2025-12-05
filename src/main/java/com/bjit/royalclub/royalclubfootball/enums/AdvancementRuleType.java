package com.bjit.royalclub.royalclubfootball.enums;

public enum AdvancementRuleType {
    TOP_N,              // Top N teams from group
    WINNER,             // Match winner
    LOSER,              // Match loser (for double elimination)
    BEST_THIRD_PLACE,   // Best 3rd place teams across groups
    ALL_TEAMS           // All teams advance (rare)
}
