package com.undoschool.booking_system.service;

import com.undoschool.booking_system.entity.*;
import com.undoschool.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class TeacherService {

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private TimezoneService timezoneService;

    public Teacher validateAndGetTeacher(Long teacherId) {
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Teacher not found with id: " + teacherId));
    }

    @Transactional
    public Offering createOffering(Offering offering) {
        // Validate teacher exists
        teacherRepository.findById(offering.getTeacherId())
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        // Validate course exists
        courseRepository.findById(offering.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        offering.setCreatedAt(timezoneService.nowUTC());
        return offeringRepository.save(offering);
    }

    /**
     * Add sessions to an offering
     * Sessions should be provided in teacher's local time, converted to UTC
     */
    @Transactional
    public List<Session> addSessions(Long offeringId, List<SessionRequest> sessionRequests, String teacherTimezone) {
        offeringRepository.findById(offeringId).orElseThrow(() -> new EntityNotFoundException("Offering not found"));

        List<Session> sessions = sessionRequests.stream().map(req -> {
            Session session = new Session();
            session.setOfferingId(offeringId);

            // Parse as LocalDateTime (no timezone), then apply teacher's timezone
            LocalDateTime localStart = LocalDateTime.parse(req.getStartTime());
            LocalDateTime localEnd = LocalDateTime.parse(req.getEndTime());

            ZonedDateTime zonedStart = localStart.atZone(ZoneId.of(teacherTimezone));
            ZonedDateTime zonedEnd = localEnd.atZone(ZoneId.of(teacherTimezone));

            session.setStartTime(zonedStart.toInstant());
            session.setEndTime(zonedEnd.toInstant());
            session.setCreatedAt(timezoneService.nowUTC());

            return session;
        }).toList();

        return sessionRepository.saveAll(sessions);
    }

    /**
     * Get all offerings for a teacher
     */
    @Transactional(readOnly = true)
    public List<Offering> getTeacherOfferings(Long teacherId) {
        teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

        List<Offering> offerings = offeringRepository.findByTeacherId(teacherId);

        // Load sessions for each offering
        for (Offering offering : offerings) {
            List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
            offering.setSessions(sessions);
        }

        return offerings;
    }

    // Inner class for session request
    public static class SessionRequest {
        public String startTime; // ISO format: "2026-06-07T18:00:00"
        public String endTime; // ISO format: "2026-06-07T19:00:00"

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }
}
