package com.offshore.platform.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    private DateTimeUtils() {
    }

    public static String nowText() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }
}
