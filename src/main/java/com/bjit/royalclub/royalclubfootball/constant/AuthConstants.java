package com.bjit.royalclub.royalclubfootball.constant;

public class AuthConstants {
    private AuthConstants() {
    }

    public static final int PASSWORD_EXPIRY_DAYS = 90; // 3 months

    /**
     * At least 8 characters with a lowercase letter, an uppercase letter and a digit - the same rule
     * the reset screens state to the member.
     */
    public static final String PASSWORD_STRENGTH_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
}

