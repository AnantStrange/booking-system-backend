package com.undoschool.booking_system.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "offering_id")
    private Long offeringId;
    
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    
    @Column(name = "end_time", nullable = false)
    private Instant endTime;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @ManyToOne
    @JoinColumn(name = "offering_id", insertable = false, updatable = false)
    private Offering offering;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public ZonedDateTime getStartTimeInParentTimezone(String parentTimezone) {
        return startTime.atZone(ZoneId.of(parentTimezone));
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }
    
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Offering getOffering() { return offering; }
    public void setOffering(Offering offering) { this.offering = offering; }
}
