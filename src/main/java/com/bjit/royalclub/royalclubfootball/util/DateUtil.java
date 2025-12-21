package com.bjit.royalclub.royalclubfootball.util;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String getCurrentYearMonth() {
        YearMonth currentYearMonth = YearMonth.now();
        return currentYearMonth.format(
                DateTimeFormatter.ofPattern("yyyyMM"));
    }

    public static int parseYear(String year) throws IllegalArgumentException {
        if (year == null || year.trim().isEmpty()) {
            throw new IllegalArgumentException("Year cannot be null or empty");
        }

        try {
            return Integer.parseInt(year.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid year format. Expected format: 'YYYY' (e.g., '2025'). Provided: " + year);
        }
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }
}
