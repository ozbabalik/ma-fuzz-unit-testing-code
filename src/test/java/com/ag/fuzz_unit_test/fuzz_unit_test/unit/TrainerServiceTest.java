package com.ag.fuzz_unit_test.fuzz_unit_test.unit;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.TrainerRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer trainer1;
    private Trainer trainer2;
    private Course activeCourse;
    private Course completedCourse;

    @BeforeEach
    void setUp() {
        // Setup trainers
        trainer1 = new Trainer();
        trainer1.setId(1L);
        trainer1.setFirstName("John");
        trainer1.setLastName("Doe");
        trainer1.setEmail("john.doe@example.com");
        trainer1.setQualification("Java Expert");
        trainer1.setCourses(new ArrayList<>());
        
        trainer2 = new Trainer();
        trainer2.setId(2L);
        trainer2.setFirstName("Jane");
        trainer2.setLastName("Smith");
        trainer2.setEmail("jane.smith@example.com");
        trainer2.setQualification("Python Expert");
        trainer2.setCourses(new ArrayList<>());
        
        // Setup courses
        activeCourse = new Course();
        activeCourse.setId(1L);
        activeCourse.setName("Active Course");
        activeCourse.setStatus(CourseStatus.ACTIVE);
        activeCourse.setTrainer(trainer1);
        
        completedCourse = new Course();
        completedCourse.setId(2L);
        completedCourse.setName("Completed Course");
        completedCourse.setStatus(CourseStatus.COMPLETED);
        completedCourse.setTrainer(trainer1);
    }

    @Test
    void createTrainer_WithUniqueEmail_ShouldCreateTrainer() {
        // Arrange
        Trainer newTrainer = new Trainer();
        newTrainer.setFirstName("New");
        newTrainer.setLastName("Trainer");
        newTrainer.setEmail("new.trainer@example.com");
        newTrainer.setQualification("New Skills");
        
        when(trainerRepository.findAll()).thenReturn(Arrays.asList(trainer1, trainer2));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(newTrainer);
        
        // Act
        Trainer result = trainerService.createTrainer(newTrainer);
        
        // Assert
        assertNotNull(result);
        assertEquals("New", result.getFirstName());
        assertEquals("new.trainer@example.com", result.getEmail());
        verify(trainerRepository).findAll();
        verify(trainerRepository).save(newTrainer);
    }
    
    @Test
    void createTrainer_WithExistingEmail_ShouldThrowException() {
        // Arrange
        Trainer newTrainer = new Trainer();
        newTrainer.setFirstName("New");
        newTrainer.setLastName("Trainer");
        newTrainer.setEmail("john.doe@example.com"); // Existing email
        newTrainer.setQualification("New Skills");
        
        when(trainerRepository.findAll()).thenReturn(Arrays.asList(trainer1, trainer2));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> trainerService.createTrainer(newTrainer));
        verify(trainerRepository).findAll();
        verify(trainerRepository, never()).save(any(Trainer.class));
    }

    @Test
    void updateTrainer_WhenTrainerExists_ShouldUpdateTrainer() {
        // Arrange
        Trainer updatedDetails = new Trainer();
        updatedDetails.setFirstName("Updated John");
        updatedDetails.setLastName("Updated Doe");
        updatedDetails.setEmail("updated.john@example.com");
        updatedDetails.setQualification("Updated Skills");
        
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        when(trainerRepository.findAll()).thenReturn(Arrays.asList(trainer1, trainer2));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Trainer result = trainerService.updateTrainer(1L, updatedDetails);
        
        // Assert
        assertEquals("Updated John", result.getFirstName());
        assertEquals("Updated Doe", result.getLastName());
        assertEquals("updated.john@example.com", result.getEmail());
        assertEquals("Updated Skills", result.getQualification());
        verify(trainerRepository).findById(1L);
        verify(trainerRepository).findAll();
        verify(trainerRepository).save(any(Trainer.class));
    }
    
    @Test
    void updateTrainer_WhenTrainerDoesNotExist_ShouldThrowException() {
        // Arrange
        Trainer updatedDetails = new Trainer();
        updatedDetails.setFirstName("Updated");
        
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trainerService.updateTrainer(99L, updatedDetails));
        verify(trainerRepository).findById(99L);
        verify(trainerRepository, never()).save(any(Trainer.class));
    }
    
    @Test
    void updateTrainer_WithExistingEmail_ShouldThrowException() {
        // Arrange
        Trainer updatedDetails = new Trainer();
        updatedDetails.setFirstName("Updated John");
        updatedDetails.setLastName("Updated Doe");
        updatedDetails.setEmail("jane.smith@example.com"); // Email of trainer2
        updatedDetails.setQualification("Updated Skills");
        
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        when(trainerRepository.findAll()).thenReturn(Arrays.asList(trainer1, trainer2));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> trainerService.updateTrainer(1L, updatedDetails));
        verify(trainerRepository).findById(1L);
        verify(trainerRepository).findAll();
        verify(trainerRepository, never()).save(any(Trainer.class));
    }
    
    @Test
    void updateTrainer_WithActiveCourses_ShouldStillUpdate() {
        // Arrange
        trainer1.getCourses().add(activeCourse);
        
        Trainer updatedDetails = new Trainer();
        updatedDetails.setFirstName("Updated John");
        updatedDetails.setLastName("Updated Doe");
        updatedDetails.setEmail("updated.john@example.com");
        updatedDetails.setQualification("Updated Skills");
        
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        when(trainerRepository.findAll()).thenReturn(Arrays.asList(trainer1, trainer2));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Trainer result = trainerService.updateTrainer(1L, updatedDetails);
        
        // Assert
        assertEquals("Updated John", result.getFirstName());
        assertEquals("Updated Doe", result.getLastName());
        assertEquals("updated.john@example.com", result.getEmail());
        assertEquals("Updated Skills", result.getQualification());
    }

    @Test
    void getAllTrainers_ShouldReturnAllTrainers() {
        // Arrange
        when(trainerRepository.findAll()).thenReturn(Arrays.asList(trainer1, trainer2));
        
        // Act
        List<Trainer> result = trainerService.getAllTrainers();
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(trainer1));
        assertTrue(result.contains(trainer2));
        verify(trainerRepository).findAll();
    }

    @Test
    void getTrainerById_WhenTrainerExists_ShouldReturnTrainer() {
        // Arrange
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        
        // Act
        Trainer result = trainerService.getTrainerById(1L);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(trainerRepository).findById(1L);
    }
    
    @Test
    void getTrainerById_WhenTrainerDoesNotExist_ShouldThrowException() {
        // Arrange
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trainerService.getTrainerById(99L));
        verify(trainerRepository).findById(99L);
    }

    @Test
    void canDeleteTrainer_WithNoCourses_ShouldReturnTrue() {
        // Arrange
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        
        // Act
        boolean result = trainerService.canDeleteTrainer(1L);
        
        // Assert
        assertTrue(result);
        verify(trainerRepository).findById(1L);
    }
    
    @Test
    void canDeleteTrainer_WithCourses_ShouldReturnFalse() {
        // Arrange
        trainer1.getCourses().add(activeCourse);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        
        // Act
        boolean result = trainerService.canDeleteTrainer(1L);
        
        // Assert
        assertFalse(result);
        verify(trainerRepository).findById(1L);
    }
    
    @Test
    void canDeleteTrainer_WhenTrainerDoesNotExist_ShouldThrowException() {
        // Arrange
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trainerService.canDeleteTrainer(99L));
        verify(trainerRepository).findById(99L);
    }

    @Test
    void deleteTrainer_WithNoActiveCourses_ShouldDeleteTrainer() {
        // Arrange
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        doNothing().when(trainerRepository).deleteById(1L);
        
        // Act
        trainerService.deleteTrainer(1L);
        
        // Assert
        verify(trainerRepository).findById(1L);
        verify(trainerRepository).deleteById(1L);
    }
    
    @Test
    void deleteTrainer_WithCompletedCourses_ShouldDeleteTrainer() {
        // Arrange
        trainer1.getCourses().add(completedCourse);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        doNothing().when(trainerRepository).deleteById(1L);
        
        // Act
        trainerService.deleteTrainer(1L);
        
        // Assert
        verify(trainerRepository).findById(1L);
        verify(trainerRepository).deleteById(1L);
    }
    
    @Test
    void deleteTrainer_WithActiveCourses_ShouldThrowException() {
        // Arrange
        trainer1.getCourses().add(activeCourse);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> trainerService.deleteTrainer(1L));
        verify(trainerRepository).findById(1L);
        verify(trainerRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void deleteTrainer_WithPlannedCourses_ShouldThrowException() {
        // Arrange
        Course plannedCourse = new Course();
        plannedCourse.setId(3L);
        plannedCourse.setName("Planned Course");
        plannedCourse.setStatus(CourseStatus.PLANNED);
        plannedCourse.setTrainer(trainer1);
        
        trainer1.getCourses().add(plannedCourse);
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer1));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> trainerService.deleteTrainer(1L));
        verify(trainerRepository).findById(1L);
        verify(trainerRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void deleteTrainer_WhenTrainerDoesNotExist_ShouldThrowException() {
        // Arrange
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> trainerService.deleteTrainer(99L));
        verify(trainerRepository).findById(99L);
        verify(trainerRepository, never()).deleteById(anyLong());
    }
} 