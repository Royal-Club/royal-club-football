package com.bjit.royalclub.royalclubfootball.util;

public class StringUtils {

    private StringUtils() {

    }

    public static String normalizeString(String input) {
        return (input == null || input.isBlank()) ? null : input;
    }
}
