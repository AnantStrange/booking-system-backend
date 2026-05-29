package com.undoschool.booking_system.entity;

import jakarta.persistence.*;
import java.util.List;
import java.time.Instant;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @OneToMany(targetEntity = Offering.class, mappedBy = "course")
    private List<Offering> offerings;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public List<Offering> getOfferings() { return offerings; }
    public void setOfferings(List<Offering> offerings) { this.offerings = offerings; }
}
