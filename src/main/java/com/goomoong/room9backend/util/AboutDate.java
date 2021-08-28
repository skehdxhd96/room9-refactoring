package com.goomoong.room9backend.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AboutDate {
    public static LocalDateTime getLocalDateTimeFromString(String date) {
        LocalDate selectDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime currentTime = LocalTime.now();
        LocalDateTime returnDate = LocalDateTime.of(selectDate, currentTime);
        return returnDate;
    }

    public static long compareDay(String date1, String date2) {
        LocalDate startDate = LocalDate.parse(date1, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate finalDate = LocalDate.parse(date2, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        long days = ChronoUnit.DAYS.between(startDate, finalDate);
        return days;
    }
}
