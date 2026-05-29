package com.undoschool.booking_system.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimezoneService {
    
    // Convert from teacher's local time to UTC for storage
    public Instant toUTC(ZonedDateTime localTime, String timezone) {
        ZonedDateTime zoned = localTime.withZoneSameInstant(ZoneId.of(timezone));
        return zoned.withZoneSameInstant(ZoneId.of("UTC")).toInstant();
    }
    
    // Convert from UTC to parent's local time for display
    public ZonedDateTime toLocal(Instant utcTime, String timezone) {
        return utcTime.atZone(ZoneId.of(timezone));
    }
    
    // Format for API responses
    public String formatTime(Instant utcTime, String timezone) {
        ZonedDateTime local = toLocal(utcTime, timezone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return local.format(formatter);
    }
    
    // Get current time in UTC
    public Instant nowUTC() {
        return Instant.now();
    }
}
