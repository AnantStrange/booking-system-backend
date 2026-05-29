package com.undoschool.booking_system.repository;

import com.undoschool.booking_system.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByParentId(Long parentId);
    
    @Query("SELECT b FROM Booking b WHERE b.parentId = :parentId")
    List<Booking> findBookingsByParentId(@Param("parentId") Long parentId);

    boolean existsByParentIdAndOfferingId(Long parentId, Long offeringId);

}
