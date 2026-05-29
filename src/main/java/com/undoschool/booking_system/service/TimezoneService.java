package com.undoschool.booking_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.undoschool.booking_system.repository.ParentRepository;
import com.undoschool.booking_system.repository.TeacherRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimezoneService {

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    public Instant nowUTC() {
        return Instant.now();
    }

    public Instant toUTC(ZonedDateTime localTime, String timezone) {
        ZonedDateTime zoned = localTime.withZoneSameInstant(ZoneId.of(timezone));
        return zoned.withZoneSameInstant(ZoneId.of("UTC")).toInstant();
    }

    public ZonedDateTime toLocal(Instant utcTime, String timezone) {
        return utcTime.atZone(ZoneId.of(timezone));
    }

    public String formatTime(Instant utcTime, String timezone) {
        ZonedDateTime local = toLocal(utcTime, timezone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return local.format(formatter);
    }

    public String getLocalTimeStr(Long id, String type, Instant utcTime) {
        String timezone;

        if (type.equals("parent")) {
            timezone = parentRepository.findById(id).get().getTimezone();
        } else {
            timezone = teacherRepository.findById(id).get().getTimezone();
        }

        return utcTime.atZone(ZoneId.of(timezone))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }

    public ZonedDateTime getParentLocalTime(Long id, String type) {
        String timezone;

        if (type.equals("parent")) {
            timezone = parentRepository.findById(id).get().getTimezone();
        } else {
            timezone = teacherRepository.findById(id).get().getTimezone();
        }

        return ZonedDateTime.now(ZoneId.of(timezone));
    }

    public String getParentLocalTimeStr(Long id, String type) {
        ZonedDateTime zonedDateTime = getParentLocalTime(id, type);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
    }
}
