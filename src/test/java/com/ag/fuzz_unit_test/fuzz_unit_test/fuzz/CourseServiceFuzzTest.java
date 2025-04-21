package com.ag.fuzz_unit_test.fuzz_unit_test.fuzz;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.TrainerRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.CourseService;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class CourseServiceFuzzTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private CourseService courseService;

    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        // Setup default mocks
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course c = invocation.getArgument(0);
            if (c.getId() == null) {
                c.setId(1L);
            }
            return c;
        });
    }

    @FuzzTest
    void fuzzCreateCourse(FuzzedDataProvider data) {
        // Generate random course data
        String name = data.consumeString(30);
        String description = data.consumeString(100);
        
        // Generate dates with reasonable bounds to avoid overflow
        int startOffset = data.consumeInt(0, 1000);
        int duration = data.consumeInt(1, 100);
        LocalDate startDate = LocalDate.now().plusDays(startOffset);
        LocalDate endDate = startDate.plusDays(duration);
        
        // Sometimes generate invalid date ranges
        if (data.consumeBoolean()) {
            // Swap dates to create an invalid date range
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }
        
        Integer maxSeats = data.consumeInt(0, 100);
        CourseStatus status = Arrays.asList(CourseStatus.values())
                .get(data.consumeInt(0, CourseStatus.values().length - 1));

        // Create a course with the generated data
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        course.setStartDate(startDate);
        course.setEndDate(endDate);
        course.setMaxSeats(maxSeats);
        course.setStatus(status);
        course.setBookings(new ArrayList<>());

        // Validate the course
        Set<ConstraintViolation<Course>> violations = validator.validate(course);

        try {
            // Attempt to create the course
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            } else {
                try {
                    courseService.createCourse(course);
                    // If we get here, creation was successful
                } catch (BusinessException e) {
                    // Expected if there are business rule violations (e.g., end date before start date)
                }
            }
        } catch (ConstraintViolationException | BusinessException e) {
            // These exceptions are expected and handled
        } catch (Exception e) {
            // Any other exception is unexpected and should be reported
            throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    @FuzzTest
    void fuzzAssignTrainer(FuzzedDataProvider data) {
        // Setup course and trainer with various states
        boolean courseExists = data.consumeBoolean();
        boolean trainerExists = data.consumeBoolean();
        CourseStatus courseStatus = Arrays.asList(CourseStatus.values())
                .get(data.consumeInt(0, CourseStatus.values().length - 1));
        boolean alreadyHasTrainer = data.consumeBoolean();

        // Create course
        Course course = new Course();
        course.setId(1L);
        course.setName("Test Course");
        course.setStartDate(LocalDate.now().plusDays(10));
        course.setEndDate(LocalDate.now().plusDays(20));
        course.setMaxSeats(10);
        course.setStatus(courseStatus);
        
        // Create trainer
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setFirstName("Test");
        trainer.setLastName("Trainer");
        trainer.setEmail("trainer@example.com");
        
        if (alreadyHasTrainer) {
            Trainer existingTrainer = new Trainer();
            existingTrainer.setId(2L);
            course.setTrainer(existingTrainer);
        }

        // Setup mocks
        if (courseExists) {
            when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        } else {
            when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
        }
        
        if (trainerExists) {
            when(trainerRepository.findById(anyLong())).thenReturn(Optional.of(trainer));
        } else {
            when(trainerRepository.findById(anyLong())).thenReturn(Optional.empty());
        }

        try {
            // Attempt to assign trainer
            courseService.assignTrainer(1L, 1L);
        } catch (BusinessException e) {
            // Expected if there are business rule violations 
            // (e.g., can't assign trainer to completed course)
        } catch (Exception e) {
            // ResourceNotFoundException is expected if course or trainer doesn't exist
            if (!courseExists || !trainerExists) {
                if (!(e.getMessage() != null && e.getMessage().contains("not found"))) {
                    throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
                }
            } else {
                throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
            }
        }
    }
} 