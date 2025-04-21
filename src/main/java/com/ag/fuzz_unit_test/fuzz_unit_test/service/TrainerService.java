package com.ag.fuzz_unit_test.fuzz_unit_test.service;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.TrainerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    /**
     * Create a new trainer
     *
     * @param trainer the trainer to create
     * @return the created trainer with ID
     */
    @Transactional
    public Trainer createTrainer(@Valid @NotNull Trainer trainer) {
        // Validate unique email
        Optional<Trainer> existingTrainer = trainerRepository.findAll().stream()
                .filter(t -> t.getEmail().equals(trainer.getEmail()))
                .findFirst();
                
        if (existingTrainer.isPresent()) {
            throw new BusinessException("A trainer with email " + trainer.getEmail() + " already exists");
        }

        return trainerRepository.save(trainer);
    }

    /**
     * Update an existing trainer
     *
     * @param id the ID of the trainer to update
     * @param trainerDetails the updated trainer details
     * @return the updated trainer
     */
    @Transactional
    public Trainer updateTrainer(Long id, @Valid @NotNull Trainer trainerDetails) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));

        // Check if email is being changed and already exists
        if (!trainer.getEmail().equals(trainerDetails.getEmail())) {
            Optional<Trainer> existingTrainer = trainerRepository.findAll().stream()
                    .filter(t -> t.getEmail().equals(trainerDetails.getEmail()))
                    .findFirst();
                    
            if (existingTrainer.isPresent()) {
                throw new BusinessException("A trainer with email " + trainerDetails.getEmail() + " already exists");
            }
        }

        // Check if trainer has active courses before update
        boolean hasActiveCourses = trainer.getCourses().stream()
                .anyMatch(course -> course.getStatus() == CourseStatus.ACTIVE);

        trainer.setFirstName(trainerDetails.getFirstName());
        trainer.setLastName(trainerDetails.getLastName());
        trainer.setEmail(trainerDetails.getEmail());
        trainer.setQualification(trainerDetails.getQualification());

        return trainerRepository.save(trainer);
    }

    /**
     * Get all trainers
     *
     * @return list of all trainers
     */
    @Transactional(readOnly = true)
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    /**
     * Get trainer by ID
     *
     * @param id the trainer ID
     * @return the trainer
     */
    @Transactional(readOnly = true)
    public Trainer getTrainerById(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));
    }

    /**
     * Check if a trainer can be deleted
     *
     * @param id the trainer ID
     * @return true if the trainer can be deleted, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean canDeleteTrainer(Long id) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));
        
        // Check if trainer has any courses
        if (!trainer.getCourses().isEmpty()) {
            return false;
        }
        
        return true;
    }

    /**
     * Delete a trainer
     *
     * @param id the trainer ID
     */
    @Transactional
    public void deleteTrainer(Long id) {
        Trainer trainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found with id: " + id));
        
        // Check if trainer has any courses
        if (!trainer.getCourses().isEmpty()) {
            List<Course> activeCourses = trainer.getCourses().stream()
                    .filter(course -> course.getStatus() == CourseStatus.ACTIVE || 
                                       course.getStatus() == CourseStatus.PLANNED)
                    .toList();
            
            if (!activeCourses.isEmpty()) {
                throw new BusinessException("Cannot delete trainer with active or planned courses");
            }
        }
        
        trainerRepository.deleteById(id);
    }
} 