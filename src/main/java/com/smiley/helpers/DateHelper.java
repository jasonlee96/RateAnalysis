package com.smiley.helpers;

import org.springframework.cglib.core.Local;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateHelper {

    public static Date AddDays(Date date, int days){
            return Date.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .plusDays(days)
                    .atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate AddDays(LocalDate date, int days){
        return date.plusDays(1);
    }

    public static String ToDateString(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return formatter.format(date);
    }

    public static Instant ParseDate(String datetime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the string into LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(datetime, formatter);

        // Convert LocalDateTime to Instant (assuming system timezone)
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
