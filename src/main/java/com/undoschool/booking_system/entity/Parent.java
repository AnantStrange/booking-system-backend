package com.undoschool.booking_system.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "parents")
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String timezone;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @OneToMany(mappedBy = "parentId")
    private List<Booking> bookings;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    public ZonedDateTime getCreatedAtInParentTimezone() {
        return createdAt.atZone(ZoneId.of(timezone));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
}
