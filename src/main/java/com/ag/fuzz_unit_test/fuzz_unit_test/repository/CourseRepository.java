package com.ag.fuzz_unit_test.fuzz_unit_test.repository;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    /**
     * Find all courses with a specific status
     * 
     * @param status the course status to filter by
     * @return a list of courses with the given status
     */
    List<Course> findByCourseStatus(CourseStatus status);
} 