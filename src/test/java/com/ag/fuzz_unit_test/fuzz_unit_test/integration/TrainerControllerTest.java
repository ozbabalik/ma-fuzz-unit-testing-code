package com.ag.fuzz_unit_test.fuzz_unit_test.integration;

import com.ag.fuzz_unit_test.fuzz_unit_test.controller.TrainerController;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.TrainerDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import com.ag.fuzz_unit_test.fuzz_unit_test.mapper.TrainerMapper;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
public class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainerService trainerService;

    @MockBean
    private TrainerMapper trainerMapper;

    private Trainer trainer1;
    private Trainer trainer2;
    private TrainerDto trainerDto1;
    private TrainerDto trainerDto2;
    private TrainerDto newTrainerDto;

    @BeforeEach
    void setUp() {
        // Setup trainers
        trainer1 = new Trainer();
        trainer1.setId(1L);
        trainer1.setFirstName("John");
        trainer1.setLastName("Doe");
        trainer1.setEmail("john.doe@example.com");
        trainer1.setQualification("Java Expert");

        trainer2 = new Trainer();
        trainer2.setId(2L);
        trainer2.setFirstName("Jane");
        trainer2.setLastName("Smith");
        trainer2.setEmail("jane.smith@example.com");
        trainer2.setQualification("Python Expert");
        
        // Setup trainer DTOs
        trainerDto1 = new TrainerDto();
        trainerDto1.setId(1L);
        trainerDto1.setFirstName("John");
        trainerDto1.setLastName("Doe");
        trainerDto1.setEmail("john.doe@example.com");
        trainerDto1.setQualification("Java Expert");
        
        trainerDto2 = new TrainerDto();
        trainerDto2.setId(2L);
        trainerDto2.setFirstName("Jane");
        trainerDto2.setLastName("Smith");
        trainerDto2.setEmail("jane.smith@example.com");
        trainerDto2.setQualification("Python Expert");
        
        newTrainerDto = new TrainerDto();
        newTrainerDto.setFirstName("New");
        newTrainerDto.setLastName("Trainer");
        newTrainerDto.setEmail("new.trainer@example.com");
        newTrainerDto.setQualification("New Skills");
        
        // Setup mapper mock responses
        when(trainerMapper.toDto(trainer1)).thenReturn(trainerDto1);
        when(trainerMapper.toDto(trainer2)).thenReturn(trainerDto2);
        when(trainerMapper.toDtoList(Arrays.asList(trainer1, trainer2)))
                .thenReturn(Arrays.asList(trainerDto1, trainerDto2));
    }

    @Test
    void getAllTrainers_ShouldReturnTrainerList() throws Exception {
        List<Trainer> trainers = Arrays.asList(trainer1, trainer2);
        when(trainerService.getAllTrainers()).thenReturn(trainers);

        mockMvc.perform(get("/api/trainers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].firstName", is("Jane")));
    }

    @Test
    void getTrainerById_WhenTrainerExists_ShouldReturnTrainer() throws Exception {
        when(trainerService.getTrainerById(1L)).thenReturn(trainer1);

        mockMvc.perform(get("/api/trainers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.qualification", is("Java Expert")));
    }

    @Test
    void createTrainer_WhenValid_ShouldReturnCreated() throws Exception {
        Trainer newTrainer = new Trainer();
        newTrainer.setFirstName("New");
        newTrainer.setLastName("Trainer");
        newTrainer.setEmail("new.trainer@example.com");
        newTrainer.setQualification("New Skills");
        
        Trainer createdTrainer = new Trainer();
        createdTrainer.setId(3L);
        createdTrainer.setFirstName("New");
        createdTrainer.setLastName("Trainer");
        createdTrainer.setEmail("new.trainer@example.com");
        createdTrainer.setQualification("New Skills");
        
        TrainerDto createdTrainerDto = new TrainerDto();
        createdTrainerDto.setId(3L);
        createdTrainerDto.setFirstName("New");
        createdTrainerDto.setLastName("Trainer");
        createdTrainerDto.setEmail("new.trainer@example.com");
        createdTrainerDto.setQualification("New Skills");
        
        when(trainerMapper.toEntity(newTrainerDto)).thenReturn(newTrainer);
        when(trainerService.createTrainer(newTrainer)).thenReturn(createdTrainer);
        when(trainerMapper.toDto(createdTrainer)).thenReturn(createdTrainerDto);

        mockMvc.perform(post("/api/trainers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTrainerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.firstName", is("New")))
                .andExpect(jsonPath("$.lastName", is("Trainer")))
                .andExpect(jsonPath("$.email", is("new.trainer@example.com")))
                .andExpect(jsonPath("$.qualification", is("New Skills")));
    }

    @Test
    void updateTrainer_WhenValid_ShouldReturnUpdatedTrainer() throws Exception {
        Trainer updateTrainer = new Trainer();
        updateTrainer.setFirstName("Updated");
        updateTrainer.setLastName("Trainer");
        updateTrainer.setEmail("updated.trainer@example.com");
        updateTrainer.setQualification("Updated Skills");
        
        Trainer updatedTrainer = new Trainer();
        updatedTrainer.setId(1L);
        updatedTrainer.setFirstName("Updated");
        updatedTrainer.setLastName("Trainer");
        updatedTrainer.setEmail("updated.trainer@example.com");
        updatedTrainer.setQualification("Updated Skills");
        
        TrainerDto updateTrainerDto = new TrainerDto();
        updateTrainerDto.setFirstName("Updated");
        updateTrainerDto.setLastName("Trainer");
        updateTrainerDto.setEmail("updated.trainer@example.com");
        updateTrainerDto.setQualification("Updated Skills");
        
        TrainerDto updatedTrainerDto = new TrainerDto();
        updatedTrainerDto.setId(1L);
        updatedTrainerDto.setFirstName("Updated");
        updatedTrainerDto.setLastName("Trainer");
        updatedTrainerDto.setEmail("updated.trainer@example.com");
        updatedTrainerDto.setQualification("Updated Skills");
        
        when(trainerMapper.toEntity(updateTrainerDto)).thenReturn(updateTrainer);
        when(trainerService.updateTrainer(eq(1L), any(Trainer.class))).thenReturn(updatedTrainer);
        when(trainerMapper.toDto(updatedTrainer)).thenReturn(updatedTrainerDto);

        mockMvc.perform(put("/api/trainers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTrainerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("Trainer")))
                .andExpect(jsonPath("$.email", is("updated.trainer@example.com")))
                .andExpect(jsonPath("$.qualification", is("Updated Skills")));
    }

    @Test
    void deleteTrainer_ShouldReturnNoContent() throws Exception {
        doNothing().when(trainerService).deleteTrainer(1L);

        mockMvc.perform(delete("/api/trainers/1"))
                .andExpect(status().isNoContent());
        
        verify(trainerService).deleteTrainer(1L);
    }

    @Test
    void canDeleteTrainer_WhenCanDelete_ShouldReturnTrue() throws Exception {
        when(trainerService.canDeleteTrainer(1L)).thenReturn(true);

        mockMvc.perform(get("/api/trainers/1/can-delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void canDeleteTrainer_WhenCannotDelete_ShouldReturnFalse() throws Exception {
        when(trainerService.canDeleteTrainer(2L)).thenReturn(false);

        mockMvc.perform(get("/api/trainers/2/can-delete"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
} 