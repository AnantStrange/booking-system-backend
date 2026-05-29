package com.undoschool.booking_system.dto;

public class SessionInfo {
    private Long id;
	private String teacher;
    private String startTimeLocal;
    private String endTimeLocal;
    private String createdAtLocal;

    public String getTeacher() {
		return teacher;
	}
	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}
	public String getCreatedAtLocal() {
		return createdAtLocal;
	}
	public void setCreatedAtLocal(String createdAtLocal) {
		this.createdAtLocal = createdAtLocal;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getStartTimeLocal() {
		return startTimeLocal;
	}
	public void setStartTimeLocal(String startTimeLocal) {
		this.startTimeLocal = startTimeLocal;
	}
	public String getEndTimeLocal() {
		return endTimeLocal;
	}
	public void setEndTimeLocal(String endTimeLocal) {
		this.endTimeLocal = endTimeLocal;
	}
}
