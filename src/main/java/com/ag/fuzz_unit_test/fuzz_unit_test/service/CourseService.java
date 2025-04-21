package com.ag.fuzz_unit_test.fuzz_unit_test.service;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.TrainerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final TrainerRepository trainerRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, TrainerRepository trainerRepository) {
        this.courseRepository = courseRepository;
        this.trainerRepository = trainerRepository;
    }

    /**
     * Create a new course
     *
     * @param course the course to create
     * @return the created course with ID
     */
    @Transactional
    public Course createCourse(@Valid @NotNull Course course) {
        validateCourse(course);

        // Set default status if not provided
        if (course.getStatus() == null) {
            course.setStatus(CourseStatus.PLANNED);
        }

        // Verify trainer if provided
        if (course.getTrainer() != null && course.getTrainer().getId() != null) {
            Trainer trainer = trainerRepository.findById(course.getTrainer().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + course.getTrainer().getId()));
            course.setTrainer(trainer);
        }

        return courseRepository.save(course);
    }

    /**
     * Update an existing course
     *
     * @param id the ID of the course to update
     * @param courseDetails the updated course details
     * @return the updated course
     */
    @Transactional
    public Course updateCourse(Long id, @Valid @NotNull Course courseDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        validateCourse(courseDetails);
        
        // Validate status transition
        if (courseDetails.getStatus() != null && courseDetails.getStatus() != course.getStatus()) {
            validateCourseStatusTransition(course.getStatus(), courseDetails.getStatus());
        }

        course.setName(courseDetails.getName());
        course.setDescription(courseDetails.getDescription());
        course.setStartDate(courseDetails.getStartDate());
        course.setEndDate(courseDetails.getEndDate());
        course.setMaxSeats(courseDetails.getMaxSeats());
        
        if (courseDetails.getStatus() != null) {
            course.setStatus(courseDetails.getStatus());
        }

        return courseRepository.save(course);
    }

    /**
     * Assign a trainer to a course
     *
     * @param courseId the ID of the course
     * @param trainerId the ID of the trainer
     * @return the updated course
     */
    @Transactional
    public Course assignTrainer(Long courseId, Long trainerId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + trainerId));

        // Check if course can have a trainer assigned
        if (course.getStatus() == CourseStatus.COMPLETED || course.getStatus() == CourseStatus.CANCELLED) {
            throw new BusinessException("Cannot assign trainer to a completed or cancelled course");
        }

        course.setTrainer(trainer);
        return courseRepository.save(course);
    }

    /**
     * Remove a trainer from a course
     *
     * @param courseId the ID of the course
     * @return the updated course
     */
    @Transactional
    public Course removeTrainer(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Check if course already has a trainer
        if (course.getTrainer() == null) {
            throw new BusinessException("Course does not have a trainer assigned");
        }

        // Check if course can have trainer removed
        if (course.getStatus() == CourseStatus.ACTIVE) {
            throw new BusinessException("Cannot remove trainer from an active course");
        }

        if (course.getStatus() == CourseStatus.COMPLETED) {
            throw new BusinessException("Cannot remove trainer from a completed course");
        }

        course.setTrainer(null);
        return courseRepository.save(course);
    }

    /**
     * Get all courses
     *
     * @return list of all courses
     */
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Get course by ID
     *
     * @param id the course ID
     * @return the course
     */
    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    /**
     * Get courses by status
     *
     * @param status the course status
     * @return list of courses with the given status
     */
    @Transactional(readOnly = true)
    public List<Course> getCoursesByStatus(CourseStatus status) {
        return courseRepository.findByCourseStatus(status);
    }

    /**
     * Change the status of a course
     *
     * @param courseId the ID of the course
     * @param newStatus the new status to set
     * @return the updated course
     */
    @Transactional
    public Course changeCourseStatus(Long courseId, CourseStatus newStatus) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        validateCourseStatusTransition(course.getStatus(), newStatus);
        
        // Additional validation before changing to ACTIVE
        if (newStatus == CourseStatus.ACTIVE) {
            if (course.getTrainer() == null) {
                throw new BusinessException("Cannot activate a course without a trainer");
            }
        }

        course.setStatus(newStatus);
        return courseRepository.save(course);
    }

    /**
     * Validate course details
     *
     * @param course the course to validate
     */
    private void validateCourse(Course course) {
        // Check that end date is after start date
        if (course.getStartDate() != null && course.getEndDate() != null) {
            if (course.getEndDate().isBefore(course.getStartDate())) {
                throw new BusinessException("Course end date must be after start date");
            }
        }

        // Check that max seats is positive
        if (course.getMaxSeats() != null && course.getMaxSeats() <= 0) {
            throw new BusinessException("Maximum seats must be greater than zero");
        }

        // Check that start date is not in the past for new courses
        if (course.getId() == null && course.getStartDate() != null) {
            if (course.getStartDate().isBefore(LocalDate.now())) {
                throw new BusinessException("Course start date cannot be in the past");
            }
        }
    }

    /**
     * Validate course status transitions
     *
     * @param currentStatus the current course status
     * @param newStatus the new course status
     */
    private void validateCourseStatusTransition(CourseStatus currentStatus, CourseStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change, always valid
        }

        switch (currentStatus) {
            case PLANNED:
                // From PLANNED, can move to ACTIVE or CANCELLED
                if (newStatus != CourseStatus.ACTIVE && newStatus != CourseStatus.CANCELLED) {
                    throw new BusinessException("Invalid course status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case ACTIVE:
                // From ACTIVE, can move to COMPLETED or CANCELLED
                if (newStatus != CourseStatus.COMPLETED && newStatus != CourseStatus.CANCELLED) {
                    throw new BusinessException("Invalid course status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case COMPLETED:
                // From COMPLETED, can't change status
                throw new BusinessException("Cannot change status of a completed course");
            case CANCELLED:
                // From CANCELLED, can move to PLANNED only
                if (newStatus != CourseStatus.PLANNED) {
                    throw new BusinessException("Invalid course status transition from " + currentStatus + " to " + newStatus);
                }
                break;
        }
    }
} 