package com.undoschool.booking_system.repository;

import com.undoschool.booking_system.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    public String getTimeZoneById();
}
