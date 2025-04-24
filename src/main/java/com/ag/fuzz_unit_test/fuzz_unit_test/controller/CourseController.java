package com.ag.fuzz_unit_test.fuzz_unit_test.controller;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.CourseDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.mapper.CourseMapper;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.CourseService;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.TrainerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseMapper courseMapper;
    private final TrainerService trainerService;

    @Autowired
    public CourseController(CourseService courseService, CourseMapper courseMapper, 
                           TrainerService trainerService) {
        this.courseService = courseService;
        this.courseMapper = courseMapper;
        this.trainerService = trainerService;
    }

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courseMapper.toDtoList(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(courseMapper.toDto(course));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CourseDto>> getCoursesByStatus(@PathVariable CourseStatus status) {
        List<Course> courses = courseService.getCoursesByStatus(status);
        return ResponseEntity.ok(courseMapper.toDtoList(courses));
    }

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseDto courseDto) {
        Course course = courseMapper.toEntity(courseDto);
        Course createdCourse = courseService.createCourse(course);
        return new ResponseEntity<>(courseMapper.toDto(createdCourse), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        Course course = courseMapper.toEntity(courseDto);
        Course updatedCourse = courseService.updateCourse(id, course);
        return ResponseEntity.ok(courseMapper.toDto(updatedCourse));
    }

    @PostMapping("/{courseId}/trainer/{trainerId}")
    public ResponseEntity<CourseDto> assignTrainer(@PathVariable Long courseId, @PathVariable Long trainerId) {
        Course course = courseService.assignTrainer(courseId, trainerId);
        return ResponseEntity.ok(courseMapper.toDto(course));
    }

    @DeleteMapping("/{courseId}/trainer")
    public ResponseEntity<CourseDto> removeTrainer(
            @PathVariable Long courseId,
            @RequestParam(required = false) Long replacementTrainerId) {
        
        // Use existing courseService.getCourseById instead of findById
        Course course = courseService.getCourseById(courseId);
        
        if (replacementTrainerId != null) {
            // Use the replacement trainer ID from the request parameter
            course = courseService.assignTrainer(courseId, replacementTrainerId);
        } else {
            // If no replacement trainer is provided, use the service method
            course = courseService.removeTrainer(courseId);
        }
        
        return ResponseEntity.ok(courseMapper.toDto(course));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CourseDto> changeCourseStatus(@PathVariable Long id, @RequestParam CourseStatus status) {
        Course course = courseService.changeCourseStatus(id, status);
        return ResponseEntity.ok(courseMapper.toDto(course));
    }
} 
