package com.undoschool.booking_system.controller;

import com.undoschool.booking_system.dto.CourseInfo;
import com.undoschool.booking_system.dto.CreateCourseRequest;
import com.undoschool.booking_system.dto.CreateOfferingRequest;
import com.undoschool.booking_system.dto.CreateTeacherRequest;
import com.undoschool.booking_system.dto.OfferingInfo;
import com.undoschool.booking_system.dto.SessionInfo;
import com.undoschool.booking_system.dto.TeacherProfile;
import com.undoschool.booking_system.dto.TeacherResponse;
import com.undoschool.booking_system.entity.Course;
import com.undoschool.booking_system.entity.Offering;
import com.undoschool.booking_system.entity.Session;
import com.undoschool.booking_system.entity.Teacher;
import com.undoschool.booking_system.repository.CourseRepository;
import com.undoschool.booking_system.repository.OfferingRepository;
import com.undoschool.booking_system.repository.SessionRepository;
import com.undoschool.booking_system.repository.TeacherRepository;
import com.undoschool.booking_system.service.TeacherService;
import com.undoschool.booking_system.service.TimezoneService;
import com.undoschool.booking_system.service.TeacherService.SessionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private TimezoneService timezoneService;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping("/{teacherId}/offerings")
    public ResponseEntity<?> getTeacherOfferings(@PathVariable Long teacherId) {
        teacherService.validateAndGetTeacher(teacherId);

        List<Offering> offerings = offeringRepository.findByTeacherId(teacherId);

        List<OfferingInfo> result = offerings.stream().map(offering -> {
            OfferingInfo dto = new OfferingInfo();
            dto.setId(offering.getId());
            dto.setName(offering.getName());
            dto.setCourseId(offering.getCourseId());
            dto.setTeacherId(offering.getTeacherId());
            dto.setCreatedAtLocal(timezoneService.getLocalTimeStr(teacherId, "teacher", offering.getCreatedAt()));

            List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
            List<SessionInfo> sessionDTOs = sessions.stream().map(session -> {
                SessionInfo sessionDTO = new SessionInfo();
                sessionDTO.setId(session.getId());
                sessionDTO.setStartTimeLocal(session.getStartTime().toString());
                sessionDTO.setEndTimeLocal(session.getEndTime().toString());
                sessionDTO.setCreatedAtLocal(
                        timezoneService.getLocalTimeStr(teacherId, "teacher", session.getCreatedAt()));
                return sessionDTO;
            }).toList();

            dto.setSessions(sessionDTOs);
            return dto;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{teacherId}")
    public ResponseEntity<?> getTeacherProfile(@PathVariable Long teacherId) {
        Teacher teacher = teacherService.validateAndGetTeacher(teacherId);

        List<Offering> offerings = offeringRepository.findByTeacherId(teacherId);

        TeacherProfile dto = new TeacherProfile();
        dto.setId(teacher.getId());
        dto.setName(teacher.getName());
        dto.setTimezone(teacher.getTimezone());
        dto.setTotalOfferings(offerings.size());

        return ResponseEntity.ok(dto);
    }

    // 1. POST /teachers/
    @PostMapping
    public ResponseEntity<TeacherResponse> createTeacher(@RequestBody CreateTeacherRequest request) {
        Teacher teacher = new Teacher();
        teacher.setName(request.getName());
        teacher.setTimezone(request.getTimezone());

        Teacher saved = teacherRepository.save(teacher);

        TeacherResponse response = new TeacherResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setTimezone(saved.getTimezone());
        response.setCreatedAt(timezoneService.getLocalTimeStr(teacher.getId(), "teacher",
                    teacher.getCreatedAt()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. POST /teachers/{teacherId}/courses
    @PostMapping("/{teacherId}/courses")
    public ResponseEntity<CourseInfo> createCourse(
            @PathVariable Long teacherId,
            @RequestBody CreateCourseRequest request) {

        teacherService.validateAndGetTeacher(teacherId);

        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCreatedAt(java.time.Instant.now());

        Course saved = courseRepository.save(course);

        CourseInfo dto = new CourseInfo();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setDescription(saved.getDescription());
        dto.setCreatedAtLocal(timezoneService.getLocalTimeStr(teacherId, "teacher", saved.getCreatedAt()));

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // 3. POST /teachers/{teacherId}/offerings
    @PostMapping("/{teacherId}/offerings")
    public ResponseEntity<OfferingInfo> createOffering(
            @PathVariable Long teacherId,
            @RequestBody CreateOfferingRequest request) {

        teacherService.validateAndGetTeacher(teacherId);

        // Check course exists
        courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Course not found: " + request.getCourseId()));

        Offering offering = new Offering();
        offering.setName(request.getName());
        offering.setCourseId(request.getCourseId());
        offering.setTeacherId(teacherId);
        offering.setCreatedAt(java.time.Instant.now());

        Offering saved = offeringRepository.save(offering);

        OfferingInfo dto = new OfferingInfo();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setCourseId(saved.getCourseId());
        dto.setTeacherId(saved.getTeacherId());
        dto.setCreatedAtLocal(timezoneService.getLocalTimeStr(teacherId, "teacher", saved.getCreatedAt()));
        dto.setSessions(List.of()); // Empty list initially

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // 4. POST /api/teachers/offerings/{offeringId}/sessions
    @PostMapping("/{teacherId}/offerings/{offeringId}/sessions")
    public ResponseEntity<List<SessionInfo>> addSessions(
            @PathVariable Long teacherId,
            @PathVariable Long offeringId,
            @RequestBody List<SessionRequest> sessionRequests) {

        Teacher teacher = teacherService.validateAndGetTeacher(teacherId);
        offeringRepository.findById(offeringId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offering not found: " + offeringId));

        List<Session> savedSessions = teacherService.addSessions(offeringId, sessionRequests,
                teacher.getTimezone());

        List<SessionInfo> result = savedSessions.stream().map(session -> {
            SessionInfo dto = new SessionInfo();
            dto.setId(session.getId());
            dto.setStartTimeLocal(timezoneService.getLocalTimeStr(teacherId, "teacher", session.getStartTime()));
            dto.setEndTimeLocal(timezoneService.getLocalTimeStr(teacherId, "teacher", session.getEndTime()));
            dto.setCreatedAtLocal(timezoneService.getLocalTimeStr(teacherId, "teacher", session.getCreatedAt()));
            return dto;
        }).toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
