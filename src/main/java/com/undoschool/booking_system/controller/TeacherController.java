package com.undoschool.booking_system.controller;

import com.undoschool.booking_system.entity.Offering;
import com.undoschool.booking_system.entity.Session;
import com.undoschool.booking_system.service.TeacherService;
import com.undoschool.booking_system.service.TimezoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private TimezoneService timezoneService;
    
    // Create offering
    @PostMapping("/offerings")
    public ResponseEntity<Offering> createOffering(@RequestBody Offering offering) {
        Offering created = teacherService.createOffering(offering);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    // Add sessions to offering
    @PostMapping("/offerings/{offeringId}/sessions")
    public ResponseEntity<List<Session>> addSessions(
            @PathVariable Long offeringId,
            @RequestBody List<Map<String, String>> sessions,
            @RequestParam String timezone) {
        
        List<TeacherService.SessionRequest> requests = sessions.stream().map(s -> {
            TeacherService.SessionRequest req = new TeacherService.SessionRequest();
            req.startTime = s.get("startTime");
            req.endTime = s.get("endTime");
            return req;
        }).toList();
        
        List<Session> created = teacherService.addSessions(offeringId, requests, timezone);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    // Get teacher's offerings
    @GetMapping("/offerings")
    public ResponseEntity<List<Offering>> getOfferings(@RequestParam Long teacherId) {
        List<Offering> offerings = teacherService.getTeacherOfferings(teacherId);
        return ResponseEntity.ok(offerings);
    }
}
