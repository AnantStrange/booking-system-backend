package com.undoschool.booking_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.undoschool.booking_system.dto.CourseInfo;
import com.undoschool.booking_system.dto.OfferingInfo;
import com.undoschool.booking_system.dto.SessionInfo;
import com.undoschool.booking_system.entity.Course;
import com.undoschool.booking_system.entity.Offering;
import com.undoschool.booking_system.entity.Session;
import com.undoschool.booking_system.repository.CourseRepository;
import com.undoschool.booking_system.repository.OfferingRepository;
import com.undoschool.booking_system.repository.SessionRepository;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseInfo> getCourse(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found: " + courseId));

        CourseInfo dto = new CourseInfo();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setCreatedAtLocal(course.getCreatedAt().toString());

        // Get all offerings for this course
        List<Offering> offerings = offeringRepository.findByCourseId(courseId);

        List<OfferingInfo> offeringInfos = offerings.stream().map(offering -> {
            OfferingInfo offeringDto = new OfferingInfo();
            offeringDto.setId(offering.getId());
            offeringDto.setName(offering.getName());
            offeringDto.setCourseId(offering.getCourseId());
            offeringDto.setTeacherId(offering.getTeacherId());
            offeringDto.setCreatedAtLocal(offering.getCreatedAt().toString());

            // Get sessions for this offering
            List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
            List<SessionInfo> sessionInfos = sessions.stream().map(session -> {
                SessionInfo si = new SessionInfo();
                si.setId(session.getId());
                si.setStartTimeLocal(session.getStartTime().toString());
                si.setEndTimeLocal(session.getEndTime().toString());
                si.setCreatedAtLocal(session.getCreatedAt().toString());
                return si;
            }).toList();

            offeringDto.setSessions(sessionInfos);
            return offeringDto;
        }).toList();

        dto.setOfferings(offeringInfos);

        return ResponseEntity.ok(dto);
    }

    public ResponseEntity<List<CourseInfo>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();

        List<CourseInfo> result = courses.stream().map(course -> {
            CourseInfo dto = new CourseInfo();
            dto.setId(course.getId());
            dto.setName(course.getName());
            dto.setDescription(course.getDescription());
            dto.setCreatedAtLocal(course.getCreatedAt().toString());
            dto.setOfferings(List.of()); // Don't load offerings for list view
            return dto;
        }).toList();

        return ResponseEntity.ok(result);
    }
}
