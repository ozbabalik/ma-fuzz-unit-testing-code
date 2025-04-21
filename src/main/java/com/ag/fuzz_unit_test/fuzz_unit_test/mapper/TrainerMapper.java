package com.ag.fuzz_unit_test.fuzz_unit_test.mapper;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.CourseSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.TrainerDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.TrainerSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainerMapper {

    private final CourseMapper courseMapper;

    public TrainerMapper(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    public TrainerDto toDto(Trainer trainer) {
        if (trainer == null) {
            return null;
        }

        TrainerDto dto = new TrainerDto();
        dto.setId(trainer.getId());
        dto.setFirstName(trainer.getFirstName());
        dto.setLastName(trainer.getLastName());
        dto.setEmail(trainer.getEmail());
        dto.setQualification(trainer.getQualification());
        dto.setCreatedAt(trainer.getCreatedAt());

        // Convert courses to DTOs
        if (trainer.getCourses() != null) {
            List<CourseSummaryDto> courses = trainer.getCourses().stream()
                    .map(courseMapper::toSummaryDto)
                    .collect(Collectors.toList());
            dto.setCourses(courses);
        }

        return dto;
    }

    public Trainer toEntity(TrainerDto dto) {
        if (dto == null) {
            return null;
        }

        Trainer trainer = new Trainer();
        trainer.setId(dto.getId());
        trainer.setFirstName(dto.getFirstName());
        trainer.setLastName(dto.getLastName());
        trainer.setEmail(dto.getEmail());
        trainer.setQualification(dto.getQualification());

        return trainer;
    }

    public TrainerSummaryDto toSummaryDto(Trainer trainer) {
        if (trainer == null) {
            return null;
        }

        TrainerSummaryDto dto = new TrainerSummaryDto();
        dto.setId(trainer.getId());
        dto.setFirstName(trainer.getFirstName());
        dto.setLastName(trainer.getLastName());
        dto.setEmail(trainer.getEmail());
        dto.setQualification(trainer.getQualification());

        return dto;
    }

    public List<TrainerDto> toDtoList(List<Trainer> trainers) {
        return trainers.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 