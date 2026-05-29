package com.undoschool.booking_system.dto;

import java.util.List;

public class OfferingInfo {
    private Long id;
    private String name;
    private Long courseId;
    private Long teacherId;
    private List<SessionInfo> sessions;
	private String createdAtLocal;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    
    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    
    public List<SessionInfo> getSessions() { return sessions; }
    public void setSessions(List<SessionInfo> sessions) { this.sessions = sessions; }
    
    public String getCreatedAtLocal() { return createdAtLocal; }
	public void setCreatedAtLocal(String createdAtLocal) { this.createdAtLocal = createdAtLocal; }
}
