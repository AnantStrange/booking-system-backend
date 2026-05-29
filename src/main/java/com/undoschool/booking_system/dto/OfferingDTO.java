package com.undoschool.booking_system.dto;

import java.time.Instant;
import java.util.List;

public class OfferingDTO {
    private Long id;
    private String name;
    private Long courseId;
    private Long teacherId;
    private List<SessionDTO> sessions;
    private Instant createdAt;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public List<SessionDTO> getSessions() { return sessions; }
    public void setSessions(List<SessionDTO> sessions) { this.sessions = sessions; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
