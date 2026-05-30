package com.undoschool.booking_system.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.undoschool.booking_system.dto.OfferingInfo;
import com.undoschool.booking_system.dto.SessionInfo;
import com.undoschool.booking_system.entity.Offering;
import com.undoschool.booking_system.entity.Session;
import com.undoschool.booking_system.repository.OfferingRepository;
import com.undoschool.booking_system.repository.SessionRepository;

@RestController
@RequestMapping("/api/offerings")
public class OfferingController {

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @GetMapping("/{offeringId}")
    public ResponseEntity<OfferingInfo> getOffering(@PathVariable Long offeringId) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Offering not found: " + offeringId));

        OfferingInfo dto = new OfferingInfo();
        dto.setId(offering.getId());
        dto.setName(offering.getName());
        dto.setCourseId(offering.getCourseId());
        dto.setTeacherId(offering.getTeacherId());
        dto.setCreatedAtLocal(offering.getCreatedAt().toString());

        // Get sessions
        List<Session> sessions = sessionRepository.findByOfferingId(offeringId);
        List<SessionInfo> sessionInfos = sessions.stream().map(session -> {
            SessionInfo si = new SessionInfo();
            si.setId(session.getId());
            si.setStartTimeLocal(session.getStartTime().toString());
            si.setEndTimeLocal(session.getEndTime().toString());
            si.setCreatedAtLocal(session.getCreatedAt().toString());
            return si;
        }).toList();

        dto.setSessions(sessionInfos);

        return ResponseEntity.ok(dto);
    }
}
