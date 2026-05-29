package com.undoschool.booking_system.repository;

import com.undoschool.booking_system.entity.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {
    List<Offering> findByTeacherId(Long teacherId);
}
