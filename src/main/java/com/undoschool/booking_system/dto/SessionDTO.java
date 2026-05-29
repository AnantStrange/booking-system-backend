package com.undoschool.booking_system.dto;

import java.time.Instant;

public class SessionDTO {
    private Long id;
    private Long offeringId;
    private Instant startTime;
    private Instant endTime;
    private String startTimeLocal;  // For displaying in user's timezone
    private String endTimeLocal;    // For displaying in user's timezone
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }
    
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    
    public String getStartTimeLocal() { return startTimeLocal; }
    public void setStartTimeLocal(String startTimeLocal) { this.startTimeLocal = startTimeLocal; }
    
    public String getEndTimeLocal() { return endTimeLocal; }
    public void setEndTimeLocal(String endTimeLocal) { this.endTimeLocal = endTimeLocal; }
}
