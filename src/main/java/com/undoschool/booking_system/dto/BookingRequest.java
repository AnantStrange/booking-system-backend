package com.undoschool.booking_system.dto;

public class BookingRequest {
    private Long parentId;
    private Long offeringId;
    
    // Getters and setters
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    
    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }
}
