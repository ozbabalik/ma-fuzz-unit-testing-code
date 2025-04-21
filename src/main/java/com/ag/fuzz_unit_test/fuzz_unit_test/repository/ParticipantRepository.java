package com.ag.fuzz_unit_test.fuzz_unit_test.repository;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    
    /**
     * Find a participant by their email address
     * 
     * @param email the email address to search for
     * @return an Optional containing the participant if found
     */
    Optional<Participant> findByEmail(String email);
} 