package com.undoschool.booking_system.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bookings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"parent_id", "offering_id"})
})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "offering_id")
    private Long offeringId;
    
    @Column(name = "booked_at")
    private Instant bookedAt;
    
    @ManyToOne
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Parent parent;
    
    @ManyToOne
    @JoinColumn(name = "offering_id", insertable = false, updatable = false)
    private Offering offering;
    
    @PrePersist
    protected void onCreate() {
        bookedAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    
    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }
    
    public Instant getBookedAt() { return bookedAt; }
    public void setBookedAt(Instant bookedAt) { this.bookedAt = bookedAt; }
    
    public Parent getParent() { return parent; }
    public void setParent(Parent parent) { this.parent = parent; }
    
    public Offering getOffering() { return offering; }
    public void setOffering(Offering offering) { this.offering = offering; }
}
