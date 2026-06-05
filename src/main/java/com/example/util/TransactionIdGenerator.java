package com.example.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TransactionIdGenerator {

    private static final String PREFIX = "TXN";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private TransactionIdGenerator() {}

    public static String generate() {
        String datePart = LocalDateTime.now().format(FORMATTER);
        String uniquePart = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return PREFIX + datePart + uniquePart;
    }
}
