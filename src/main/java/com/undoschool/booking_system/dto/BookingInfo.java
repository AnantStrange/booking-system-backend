package com.undoschool.booking_system.dto;

public class BookingInfo {
    private Long bookingId;
    private String status;
    private String bookedAtLocal;
    private OfferingInfo offering;

	public Long getBookingId() {
		return bookingId;
	}
	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getBookedAtLocal() {
		return bookedAtLocal;
	}
	public void setBookedAtLocal(String bookedAtLocal) {
		this.bookedAtLocal = bookedAtLocal;
	}
	public OfferingInfo getOffering() {
		return offering;
	}
	public void setOffering(OfferingInfo offering) {
		this.offering = offering;
	}
}


