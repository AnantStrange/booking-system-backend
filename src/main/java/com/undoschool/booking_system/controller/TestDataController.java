package com.undoschool.booking_system.controller;

import com.undoschool.booking_system.entity.*;
import com.undoschool.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/test")
public class TestDataController {
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private ParentRepository parentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @GetMapping("/setup")
    public String setupTestData() {
        // Create teacher
        Teacher teacher = new Teacher();
        teacher.setName("John Doe");
        teacher.setTimezone("America/New_York");
        teacherRepository.save(teacher);
        
        // Create parent
        Parent parent = new Parent();
        parent.setName("Jane Parent");
        parent.setTimezone("Asia/Kolkata");
        parentRepository.save(parent);
        
        // Create course
        Course course = new Course();
        course.setName("Python Programming");
        course.setDescription("Learn Python from scratch");
        courseRepository.save(course);
        
        return "Test data created! Teacher ID: " + teacher.getId() + 
               ", Parent ID: " + parent.getId() + 
               ", Course ID: " + course.getId();
    }
}
