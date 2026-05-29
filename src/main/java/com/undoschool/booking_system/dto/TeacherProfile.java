package com.undoschool.booking_system.dto;

import java.util.List;

public class TeacherProfile {
    private Long id;
    private String name;
    private String timezone;
    private Integer totalOfferings;
    private List<CourseInfo> courses;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public Integer getTotalOfferings() {
		return totalOfferings;
	}
	public void setTotalOfferings(Integer totalOfferings) {
		this.totalOfferings = totalOfferings;
	}
	public List<CourseInfo> getCourses() {
		return courses;
	}
	public void setCourses(List<CourseInfo> courses) {
		this.courses = courses;
	}
}
