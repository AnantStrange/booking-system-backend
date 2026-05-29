package com.undoschool.booking_system.dto;

import java.util.List;

public class CourseInfo {
    private Long id;
    private String name;
    private String description;
    private String createdAtLocal;
	private List<OfferingInfo> offerings;
    
	public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCreatedAtLocal() { return createdAtLocal; }
	public void setCreatedAtLocal(String createdAtLocal) { this.createdAtLocal = createdAtLocal; }

	public List<OfferingInfo> getOfferings() { return offerings; }
	public void setOfferings(List<OfferingInfo> offerings) { this.offerings = offerings; }

}
