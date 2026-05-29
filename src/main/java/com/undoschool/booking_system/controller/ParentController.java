package com.undoschool.booking_system.controller;

import com.undoschool.booking_system.dto.BookingRequest;
import com.undoschool.booking_system.dto.OfferingDTO;
import com.undoschool.booking_system.dto.SessionDTO;
import com.undoschool.booking_system.entity.Booking;
import com.undoschool.booking_system.entity.Offering;
import com.undoschool.booking_system.entity.Session;
import com.undoschool.booking_system.repository.OfferingRepository;
import com.undoschool.booking_system.repository.SessionRepository;
import com.undoschool.booking_system.service.BookingService;
import com.undoschool.booking_system.service.TimezoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/parent")
public class ParentController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private OfferingRepository offeringRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private TimezoneService timezoneService;
    
    // Get available offerings (all offerings, you can filter later)
    @GetMapping("/offerings")
    public ResponseEntity<List<OfferingDTO>> getAvailableOfferings(
            @RequestParam String timezone) {
        
        List<Offering> offerings = offeringRepository.findAll();
        
        List<OfferingDTO> dtos = offerings.stream().map(offering -> {
            OfferingDTO dto = new OfferingDTO();
            dto.setId(offering.getId());
            dto.setName(offering.getName());
            dto.setCourseId(offering.getCourseId());
            dto.setTeacherId(offering.getTeacherId());
            dto.setCreatedAt(offering.getCreatedAt());
            
            List<Session> sessions = sessionRepository.findByOfferingId(offering.getId());
            List<SessionDTO> sessionDTOs = sessions.stream().map(session -> {
                SessionDTO sessionDTO = new SessionDTO();
                sessionDTO.setId(session.getId());
                sessionDTO.setOfferingId(session.getOfferingId());
                sessionDTO.setStartTime(session.getStartTime());
                sessionDTO.setEndTime(session.getEndTime());
                sessionDTO.setStartTimeLocal(timezoneService.formatTime(session.getStartTime(), timezone));
                sessionDTO.setEndTimeLocal(timezoneService.formatTime(session.getEndTime(), timezone));
                return sessionDTO;
            }).toList();
            
            dto.setSessions(sessionDTOs);
            return dto;
        }).toList();
        
        return ResponseEntity.ok(dtos);
    }
    
    // Book an offering
    @PostMapping("/bookings")
    public ResponseEntity<Booking> bookOffering(@RequestBody BookingRequest request) {
        Booking booking = bookingService.bookOffering(request.getParentId(), request.getOfferingId());
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }
    
    // Get parent's bookings
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getBookings(@RequestParam Long parentId) {
        List<Booking> bookings = bookingService.getParentBookings(parentId);
        return ResponseEntity.ok(bookings);
    }
}
