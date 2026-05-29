package com.undoschool.booking_system.service;

import com.undoschool.booking_system.entity.*;
import com.undoschool.booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private TimezoneService timezoneService;

    /**
     * Book an offering for a parent with concurrency handling
     * Uses SERIALIZABLE isolation to prevent race conditions
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking bookOffering(Long parentId, Long offeringId) {

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent not found with id: " + parentId));

        offeringRepository.findById(offeringId)
                .orElseThrow(() -> new EntityNotFoundException("Offering not found with id: " + offeringId));

        if (bookingRepository.existsByParentIdAndOfferingId(parentId, offeringId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,  // 409 Conflict
                    "Parent has already booked this offering"
                    );
        }

        List<Session> newSessions = sessionRepository.findByOfferingId(offeringId);
        if (newSessions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,  
                    "The requested offering has no sessions created yet !"
                    );
        }

        List<Booking> existingBookings = bookingRepository.findByParentId(parentId);

        // 6. Check for time conflicts with existing bookings
        for (Session newSession : newSessions) {
            for (Booking existingBooking : existingBookings) {
                List<Session> existingSessions = sessionRepository.findByOfferingId(existingBooking.getOfferingId());
                for (Session existingSession : existingSessions) {
                    if (hasTimeConflict(newSession, existingSession)) {
                        throw new IllegalStateException(
                                String.format("Time conflict with existing booking. Session [%s] to [%s] conflicts",
                                        timezoneService.formatTime(newSession.getStartTime(), parent.getTimezone()),
                                        timezoneService.formatTime(newSession.getEndTime(), parent.getTimezone())));
                    }
                }
            }
        }

        // 7. Create and save booking
        Booking booking = new Booking();
        booking.setParentId(parentId);
        booking.setOfferingId(offeringId);
        booking.setBookedAt(timezoneService.nowUTC());

        return bookingRepository.save(booking);
    }

    /**
     * Check if two sessions overlap in time
     */
    private boolean hasTimeConflict(Session session1, Session session2) {
        Instant start1 = session1.getStartTime();
        Instant end1 = session1.getEndTime();
        Instant start2 = session2.getStartTime();
        Instant end2 = session2.getEndTime();

        // Overlap if: session1 starts before session2 ends AND session2 starts before
        // session1 ends
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Get all bookings for a parent (with times in parent's timezone)
     */
    @Transactional(readOnly = true)
    public List<Booking> getParentBookings(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent not found"));

        List<Booking> bookings = bookingRepository.findByParentId(parentId);

        // Load sessions for each booking (for display purposes)
        for (Booking booking : bookings) {
            Offering offering = offeringRepository.findById(booking.getOfferingId()).orElse(null);
            if (offering != null) {
                List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
                offering.setSessions(sessions);
                booking.setOffering(offering);
            }
        }

        return bookings;
    }
}
