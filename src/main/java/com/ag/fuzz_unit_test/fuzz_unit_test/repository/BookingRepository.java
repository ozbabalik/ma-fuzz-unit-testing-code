package com.ag.fuzz_unit_test.fuzz_unit_test.repository;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Booking;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Find all bookings for a specific course with a specific booking status
     * 
     * @param courseId the ID of the course
     * @param status the booking status to filter by
     * @return a list of bookings matching the criteria
     */
    List<Booking> findByCourseIdAndStatus(Long courseId, BookingStatus status);
} 