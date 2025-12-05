package com.bjit.royalclub.royalclubfootball.enums;

public enum TeamPlayerRole {
    CAPTAIN,
    VICE_CAPTAIN,
    PLAYER;

    public static TeamPlayerRole getRoleOrDefault(String role) {
        if (role == null || role.trim().isEmpty()) {
            return PLAYER;
        }
        try {
            return TeamPlayerRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PLAYER;
        }
    }
}
