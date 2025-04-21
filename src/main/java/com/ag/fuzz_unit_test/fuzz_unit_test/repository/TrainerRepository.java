package com.ag.fuzz_unit_test.fuzz_unit_test.repository;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    
} 