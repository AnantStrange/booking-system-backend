package com.undoschool.booking_system.service;

import com.undoschool.booking_system.entity.Parent;
import com.undoschool.booking_system.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ParentService {
    
    @Autowired
    private ParentRepository parentRepository;
    
    public Parent validateAndGetParent(Long parentId) {
        return parentRepository.findById(parentId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Parent not found with id: " + parentId
            ));
    }

}
