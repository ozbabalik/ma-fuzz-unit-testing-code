package com.ag.fuzz_unit_test.fuzz_unit_test.unit;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.TrainerRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        // Setup trainer
        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setFirstName("John");
        trainer.setLastName("Smith");
        trainer.setEmail("john.smith@example.com");
        trainer.setQualification("Java Expert");
        
        // Setup course
        course = new Course();
        course.setId(1L);
        course.setName("Java Fundamentals");
        course.setDescription("Learn Java basics");
        course.setStartDate(LocalDate.now().plusDays(10));
        course.setEndDate(LocalDate.now().plusDays(20));
        course.setMaxSeats(15);
        course.setStatus(CourseStatus.PLANNED);
        course.setTrainer(trainer);
    }

    @Test
    void createCourse_WithValidData_ShouldCreateCourse() {
        // Arrange
        Course newCourse = new Course();
        newCourse.setName("New Course");
        newCourse.setDescription("New Description");
        newCourse.setStartDate(LocalDate.now().plusDays(5));
        newCourse.setEndDate(LocalDate.now().plusDays(15));
        newCourse.setMaxSeats(20);
        
        when(courseRepository.save(any(Course.class))).thenReturn(newCourse);
        
        // Act
        Course result = courseService.createCourse(newCourse);
        
        // Assert
        assertNotNull(result);
        assertEquals("New Course", result.getName());
        assertEquals(CourseStatus.PLANNED, result.getStatus());
        verify(courseRepository).save(newCourse);
    }
    
    @Test
    void createCourse_WithTrainer_ShouldCreateCourseWithTrainer() {
        // Arrange
        Course newCourse = new Course();
        newCourse.setName("New Course");
        newCourse.setDescription("New Description");
        newCourse.setStartDate(LocalDate.now().plusDays(5));
        newCourse.setEndDate(LocalDate.now().plusDays(15));
        newCourse.setMaxSeats(20);
        
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        newCourse.setTrainer(trainer);
        
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(this.trainer));
        when(courseRepository.save(any(Course.class))).thenReturn(newCourse);
        
        // Act
        Course result = courseService.createCourse(newCourse);
        
        // Assert
        assertNotNull(result);
        assertEquals("New Course", result.getName());
        assertEquals(CourseStatus.PLANNED, result.getStatus());
        assertNotNull(result.getTrainer());
        verify(trainerRepository).findById(1L);
        verify(courseRepository).save(newCourse);
    }
    
    @Test
    void createCourse_WithInvalidDates_ShouldThrowException() {
        // Arrange
        Course newCourse = new Course();
        newCourse.setName("New Course");
        newCourse.setStartDate(LocalDate.now().plusDays(15));
        newCourse.setEndDate(LocalDate.now().plusDays(5)); // End date before start date
        newCourse.setMaxSeats(20);
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.createCourse(newCourse));
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void createCourse_WithPastStartDate_ShouldThrowException() {
        // Arrange
        Course newCourse = new Course();
        newCourse.setName("New Course");
        newCourse.setStartDate(LocalDate.now().minusDays(5)); // Start date in the past
        newCourse.setEndDate(LocalDate.now().plusDays(5));
        newCourse.setMaxSeats(20);
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.createCourse(newCourse));
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void createCourse_WithInvalidMaxSeats_ShouldThrowException() {
        // Arrange
        Course newCourse = new Course();
        newCourse.setName("New Course");
        newCourse.setStartDate(LocalDate.now().plusDays(5));
        newCourse.setEndDate(LocalDate.now().plusDays(15));
        newCourse.setMaxSeats(0); // Invalid max seats
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.createCourse(newCourse));
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void createCourse_WithNonExistentTrainer_ShouldThrowException() {
        // Arrange
        Course newCourse = new Course();
        newCourse.setName("New Course");
        newCourse.setStartDate(LocalDate.now().plusDays(5));
        newCourse.setEndDate(LocalDate.now().plusDays(15));
        newCourse.setMaxSeats(20);
        
        Trainer trainer = new Trainer();
        trainer.setId(99L); // Non-existent trainer
        newCourse.setTrainer(trainer);
        
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.createCourse(newCourse));
        verify(trainerRepository).findById(99L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void updateCourse_WithValidData_ShouldUpdateCourse() {
        // Arrange
        Course updatedDetails = new Course();
        updatedDetails.setName("Updated Course");
        updatedDetails.setDescription("Updated Description");
        updatedDetails.setStartDate(LocalDate.now().plusDays(5));
        updatedDetails.setEndDate(LocalDate.now().plusDays(15));
        updatedDetails.setMaxSeats(25);
        updatedDetails.setStatus(CourseStatus.PLANNED);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Course result = courseService.updateCourse(1L, updatedDetails);
        
        // Assert
        assertEquals("Updated Course", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(25, result.getMaxSeats());
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }
    
    @Test
    void updateCourse_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.updateCourse(99L, new Course()));
        verify(courseRepository).findById(99L);
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void updateCourse_WithInvalidStatusTransition_ShouldThrowException() {
        // Arrange
        course.setStatus(CourseStatus.COMPLETED);
        
        Course updatedDetails = new Course();
        updatedDetails.setName("Updated Course");
        updatedDetails.setStartDate(LocalDate.now().plusDays(5));
        updatedDetails.setEndDate(LocalDate.now().plusDays(15));
        updatedDetails.setMaxSeats(25);
        updatedDetails.setStatus(CourseStatus.ACTIVE); // Invalid transition from COMPLETED
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.updateCourse(1L, updatedDetails));
        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void assignTrainer_WithValidIds_ShouldAssignTrainer() {
        // Arrange
        Course courseWithoutTrainer = new Course();
        courseWithoutTrainer.setId(1L);
        courseWithoutTrainer.setName("Java Fundamentals");
        courseWithoutTrainer.setStatus(CourseStatus.PLANNED);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(courseWithoutTrainer));
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Course result = courseService.assignTrainer(1L, 1L);
        
        // Assert
        assertNotNull(result.getTrainer());
        assertEquals(1L, result.getTrainer().getId());
        verify(courseRepository).findById(1L);
        verify(trainerRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }
    
    @Test
    void assignTrainer_WithNonExistentCourse_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.assignTrainer(99L, 1L));
        verify(courseRepository).findById(99L);
        verify(trainerRepository, never()).findById(anyLong());
    }
    
    @Test
    void assignTrainer_WithNonExistentTrainer_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.assignTrainer(1L, 99L));
        verify(courseRepository).findById(1L);
        verify(trainerRepository).findById(99L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void removeTrainer_WithValidCourse_ShouldRemoveTrainer() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Course result = courseService.removeTrainer(1L);
        
        // Assert
        assertNull(result.getTrainer());
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }
    
    @Test
    void removeTrainer_WithNonExistentCourse_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.removeTrainer(99L));
        verify(courseRepository).findById(99L);
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void removeTrainer_WithNoTrainer_ShouldThrowException() {
        // Arrange
        course.setTrainer(null);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.removeTrainer(1L));
        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void removeTrainer_WithActiveCourse_ShouldThrowException() {
        // Arrange
        course.setStatus(CourseStatus.ACTIVE);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.removeTrainer(1L));
        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void removeTrainer_WithCompletedCourse_ShouldThrowException() {
        // Arrange
        course.setStatus(CourseStatus.COMPLETED);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.removeTrainer(1L));
        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void getAllCourses_ShouldReturnAllCourses() {
        // Arrange
        List<Course> courses = Arrays.asList(course, new Course());
        when(courseRepository.findAll()).thenReturn(courses);
        
        // Act
        List<Course> result = courseService.getAllCourses();
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(course));
        verify(courseRepository).findAll();
    }

    @Test
    void getCourseById_WithExistingId_ShouldReturnCourse() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act
        Course result = courseService.getCourseById(1L);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Java Fundamentals", result.getName());
        verify(courseRepository).findById(1L);
    }
    
    @Test
    void getCourseById_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(99L));
        verify(courseRepository).findById(99L);
    }

    @Test
    void getCoursesByStatus_ShouldReturnCoursesWithMatchingStatus() {
        // Arrange
        List<Course> plannedCourses = Arrays.asList(course);
        when(courseRepository.findByStatus(CourseStatus.PLANNED)).thenReturn(plannedCourses);
        
        // Act
        List<Course> result = courseService.getCoursesByStatus(CourseStatus.PLANNED);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(CourseStatus.PLANNED, result.get(0).getStatus());
        verify(courseRepository).findByStatus(CourseStatus.PLANNED);
    }

    @Test
    void changeCourseStatus_WithValidTransition_ShouldChangeStatus() {
        // Arrange
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Course result = courseService.changeCourseStatus(1L, CourseStatus.ACTIVE);
        
        // Assert
        assertEquals(CourseStatus.ACTIVE, result.getStatus());
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }
    
    @Test
    void changeCourseStatus_WithNonExistentCourse_ShouldThrowException() {
        // Arrange
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> courseService.changeCourseStatus(99L, CourseStatus.ACTIVE));
        verify(courseRepository).findById(99L);
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void changeCourseStatus_WithInvalidTransition_ShouldThrowException() {
        // Arrange
        course.setStatus(CourseStatus.PLANNED);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.changeCourseStatus(1L, CourseStatus.COMPLETED));
        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }
    
    @Test
    void changeCourseStatus_ToActive_WithoutTrainer_ShouldThrowException() {
        // Arrange
        course.setTrainer(null);
        
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> courseService.changeCourseStatus(1L, CourseStatus.ACTIVE));
        verify(courseRepository).findById(1L);
        verify(courseRepository, never()).save(any(Course.class));
    }
} 
