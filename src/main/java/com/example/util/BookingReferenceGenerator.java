package com.example.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class BookingReferenceGenerator {

    private static final String PREFIX = "BK";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private BookingReferenceGenerator() {}

    public static String generate() {
        String datePart = LocalDateTime.now().format(FORMATTER);
        String uniquePart = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
        return PREFIX + datePart + uniquePart;
    }
}
