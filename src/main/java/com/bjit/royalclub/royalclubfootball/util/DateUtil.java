package com.bjit.royalclub.royalclubfootball.util;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String getCurrentYearMonth() {
        // Get the current year and month
        YearMonth currentYearMonth = YearMonth.now();

        // Format it as 'yyyyMM'
        return currentYearMonth.format(
                DateTimeFormatter.ofPattern("yyyyMM"));
    }
}
