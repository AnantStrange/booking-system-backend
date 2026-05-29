package com.undoschool.booking_system.controller;

import com.undoschool.booking_system.dto.*;
import com.undoschool.booking_system.entity.*;
import com.undoschool.booking_system.repository.*;
import com.undoschool.booking_system.service.BookingService;
import com.undoschool.booking_system.service.ParentService;
import com.undoschool.booking_system.service.TimezoneService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents")
public class ParentController {

    @Autowired
    private ParentService parentService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TimezoneService timezoneService;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // 1. GET /parents/{parentId}/offerings
    @GetMapping("/{parentId}/offerings")
    public ResponseEntity<?> getOfferings(@PathVariable Long parentId) {
        parentService.validateAndGetParent(parentId); // Throws 404 if not found

        List<Offering> offerings = offeringRepository.findAll();

        List<OfferingInfo> result = offerings.stream().map(offering -> {
            OfferingInfo dto = new OfferingInfo();
            dto.setId(offering.getId());
            dto.setName(offering.getName());
            dto.setCourseId(offering.getCourseId());
            dto.setTeacherId(offering.getTeacherId());
            dto.setCreatedAtLocal(timezoneService.getLocalTimeStr(parentId, "parent", offering.getCreatedAt()));

            List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
            List<SessionInfo> sessionDTOs = sessions.stream().map(session -> {
                SessionInfo sessionDTO = new SessionInfo();
                sessionDTO.setId(session.getId());
                sessionDTO.setStartTimeLocal(session.getStartTime().toString());
                sessionDTO.setEndTimeLocal(session.getEndTime().toString());
                sessionDTO
                        .setCreatedAtLocal(timezoneService.getLocalTimeStr(parentId, "parent", session.getCreatedAt()));
                return sessionDTO;
            }).toList();

            dto.setSessions(sessionDTOs);
            return dto;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // 2. GET /parents/{parentId}/sessions
    @GetMapping("/{parentId}/sessions")
    public ResponseEntity<?> getSessions(@PathVariable Long parentId) {
        parentService.validateAndGetParent(parentId);

        List<Session> allSessions = sessionRepository.findAll();

        List<SessionInfo> result = allSessions.stream().map(session -> {
            SessionInfo dto = new SessionInfo();
            dto.setId(session.getId());
            dto.setStartTimeLocal(session.getStartTime().toString());
            dto.setEndTimeLocal(session.getEndTime().toString());
            dto.setCreatedAtLocal(timezoneService.getLocalTimeStr(parentId, "parent", session.getCreatedAt()));
            return dto;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // 3. GET /parents/{parentId}/bookings
    @GetMapping("/{parentId}/bookings")
    public ResponseEntity<?> getBookings(@PathVariable Long parentId) {
        parentService.validateAndGetParent(parentId);

        List<Booking> bookings = bookingRepository.findByParentId(parentId);

        List<BookingInfo> result = bookings.stream().map(booking -> {
            BookingInfo dto = new BookingInfo();
            dto.setBookingId(booking.getId());
            dto.setStatus("CONFIRMED");
            dto.setBookedAtLocal(booking.getBookedAt().toString());

            Offering offering = offeringRepository.findById(booking.getOfferingId()).orElse(null);
            if (offering != null) {
                OfferingInfo offeringInfo = new OfferingInfo();
                offeringInfo.setId(offering.getId());
                offeringInfo.setName(offering.getName());
                offeringInfo.setCourseId(offering.getCourseId());
                offeringInfo.setTeacherId(offering.getTeacherId());
                offeringInfo.setCreatedAtLocal(
                        timezoneService.getLocalTimeStr(parentId, "parent", offering.getCreatedAt()));

                List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
                List<SessionInfo> sessionInfos = sessions.stream().map(session -> {
                    SessionInfo si = new SessionInfo();
                    si.setId(session.getId());
                    si.setStartTimeLocal(session.getStartTime().toString());
                    si.setEndTimeLocal(session.getEndTime().toString());
                    si.setCreatedAtLocal(timezoneService.getLocalTimeStr(parentId, "parent", session.getCreatedAt()));
                    return si;
                }).toList();

                offeringInfo.setSessions(sessionInfos);
                dto.setOffering(offeringInfo);
            }

            return dto;
        }).toList();

        return ResponseEntity.ok(result);
    }

    // 4. POST /parents/
    @PostMapping
    public ResponseEntity<ParentResponse> createParent(@RequestBody CreateParentRequest request) {
        Parent parent = new Parent();
        parent.setName(request.getName());
        parent.setTimezone(request.getTimezone());

        Parent saved = parentRepository.save(parent);

        ParentResponse response = new ParentResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setTimezone(saved.getTimezone());
        response.setCreatedAt(saved.getCreatedAt().toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 5. POST /parents/{parentId}/bookings
    @PostMapping("/{parentId}/bookings")
    public ResponseEntity<BookingInfo> createBooking(
            @PathVariable Long parentId,
            @RequestParam Long offeringId) {

        parentService.validateAndGetParent(parentId);

        Booking booking = bookingService.bookOffering(parentId, offeringId);
        String bookedLocalTime = timezoneService.getLocalTimeStr(parentId, "parent", booking.getBookedAt());

        // Build BookingInfo response
        BookingInfo dto = new BookingInfo();
        dto.setBookingId(booking.getId());
        dto.setStatus("CONFIRMED");
        dto.setBookedAtLocal(bookedLocalTime);

        // Get offering details
        Offering offering = offeringRepository.findById(offeringId).orElse(null);
        if (offering != null) {
            OfferingInfo offeringInfo = new OfferingInfo();
            offeringInfo.setId(offering.getId());
            offeringInfo.setName(offering.getName());
            offeringInfo.setCourseId(offering.getCourseId());
            offeringInfo.setTeacherId(offering.getTeacherId());
            offeringInfo
                    .setCreatedAtLocal(timezoneService.getLocalTimeStr(parentId, "parent", offering.getCreatedAt()));

            List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
            List<SessionInfo> sessionInfos = sessions.stream().map(session -> {
                SessionInfo si = new SessionInfo();
                si.setId(session.getId());
                si.setStartTimeLocal(session.getStartTime().toString());
                si.setEndTimeLocal(session.getEndTime().toString());
                si.setCreatedAtLocal(timezoneService.getLocalTimeStr(parentId, "parent", session.getCreatedAt()));
                return si;
            }).toList();

            offeringInfo.setSessions(sessionInfos);
            dto.setOffering(offeringInfo);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

}
