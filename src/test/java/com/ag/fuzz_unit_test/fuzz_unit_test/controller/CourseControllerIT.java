package com.ag.fuzz_unit_test.fuzz_unit_test.controller;

import com.ag.fuzz_unit_test.fuzz_unit_test.FuzzUnitTestApplication;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.CourseDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Trainer;
import com.ag.fuzz_unit_test.fuzz_unit_test.mapper.CourseMapper;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.TrainerRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {FuzzUnitTestApplication.class, TestConfig.class})
@AutoConfigureMockMvc
public class CourseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private CourseMapper courseMapper;

    @MockBean
    private UserService userService;

    private Course testCourse;
    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        // Create test trainer
        testTrainer = new Trainer();
        testTrainer.setFirstName("Jane");
        testTrainer.setLastName("Smith");
        testTrainer.setEmail("jane.smith@example.com");
        testTrainer.setQualification("Java Expert");
        testTrainer = trainerRepository.save(testTrainer);

        // Create test course
        testCourse = new Course();
        testCourse.setName("Spring Boot Masterclass");
        testCourse.setDescription("Learn Spring Boot from scratch");
        testCourse.setStartDate(LocalDate.now().plusDays(5));
        testCourse.setEndDate(LocalDate.now().plusDays(15));
        testCourse.setMaxSeats(15);
        testCourse.setStatus(CourseStatus.PLANNED);
        testCourse = courseRepository.save(testCourse);
    }

    @AfterEach
    void tearDown() {
        courseRepository.deleteAll();
        trainerRepository.deleteAll();
    }

    @Test
    void getAllCourses_ShouldReturnCoursesList() throws Exception {
        // Perform GET request
        mockMvc.perform(get("/api/courses")).andExpect(status().isOk()).andExpect(
                content().contentType(MediaType.APPLICATION_JSON)).andExpect(
                jsonPath("$", hasSize(greaterThanOrEqualTo(1)))).andExpect(
                jsonPath("$[0].name", is(testCourse.getName()))).andExpect(
                jsonPath("$[0].description", is(testCourse.getDescription()))).andExpect(
                jsonPath("$[0].status", is(testCourse.getStatus().toString())));
    }

    @Test
    void getCourseById_ShouldReturnCourse() throws Exception {
        // Perform GET request
        mockMvc
                .perform(get("/api/courses/{id}", testCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCourse.getId().intValue())))
                .andExpect(jsonPath("$.name", is(testCourse.getName())))
                .andExpect(jsonPath("$.maxSeats", is(testCourse.getMaxSeats())));
    }

    @Test
    void getCoursesByStatus_ShouldReturnFilteredCourses() throws Exception {
        // Perform GET request
        mockMvc
                .perform(get("/api/courses/status/{status}", CourseStatus.PLANNED))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].status", is(CourseStatus.PLANNED.toString())));
    }

    @Test
    void createCourse_ShouldReturnCreatedCourse() throws Exception {
        // Create DTO
        CourseDto courseDto = new CourseDto();
        courseDto.setName("Advanced Java Programming");
        courseDto.setDescription("Deep dive into advanced Java concepts");
        courseDto.setStartDate(LocalDate.now().plusDays(20));
        courseDto.setEndDate(LocalDate.now().plusDays(30));
        courseDto.setMaxSeats(20);
        courseDto.setStatus(CourseStatus.PLANNED);

        // Perform POST request
        MvcResult result = mockMvc
                .perform(post("/api/courses")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(courseDto.getName())))
                .andExpect(jsonPath("$.description", is(courseDto.getDescription())))
                .andExpect(jsonPath("$.status", is(CourseStatus.PLANNED.toString())))
                .andReturn();

        // Extract ID from response and verify course was saved in database
        CourseDto createdCourse = objectMapper.readValue(result.getResponse().getContentAsString(),
                                                         CourseDto.class);

        Optional<Course> savedCourse = courseRepository.findById(createdCourse.getId());
        assertTrue(savedCourse.isPresent());
        assertEquals(courseDto.getName(), savedCourse.get().getName());
    }

    @Test
    void updateCourse_ShouldReturnUpdatedCourse() throws Exception {
        // Create update DTO
        CourseDto updateDto = courseMapper.toDto(testCourse);
        updateDto.setName("Updated Course Name");
        updateDto.setMaxSeats(25);

        // Perform PUT request
        mockMvc
                .perform(put("/api/courses/{id}", testCourse.getId())
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCourse.getId().intValue())))
                .andExpect(jsonPath("$.name", is(updateDto.getName())))
                .andExpect(jsonPath("$.maxSeats", is(updateDto.getMaxSeats())));

        // Verify course was updated in database
        Optional<Course> updatedCourse = courseRepository.findById(testCourse.getId());
        assertTrue(updatedCourse.isPresent());
        assertEquals(updateDto.getName(), updatedCourse.get().getName());
        assertEquals(updateDto.getMaxSeats(), updatedCourse.get().getMaxSeats());
    }

    @Test
    void assignTrainer_ShouldAssignTrainerToCourse() throws Exception {
        // Perform POST request
        mockMvc.perform(post("/api/courses/{courseId}/trainer/{trainerId}", testCourse.getId(),
                             testTrainer.getId())).andExpect(status().isOk()).andExpect(
                content().contentType(MediaType.APPLICATION_JSON)).andExpect(
                jsonPath("$.id", is(testCourse.getId().intValue()))).andExpect(
                jsonPath("$.trainer.id", is(testTrainer.getId().intValue()))).andExpect(
                jsonPath("$.trainer.firstName", is(testTrainer.getFirstName())));

        // Verify trainer was assigned in database
        Optional<Course> updatedCourse = courseRepository.findById(testCourse.getId());
        assertTrue(updatedCourse.isPresent());
        assertNotNull(updatedCourse.get().getTrainer());
        assertEquals(testTrainer.getId(), updatedCourse.get().getTrainer().getId());
    }

    @Test
    void removeTrainer_ShouldRemoveTrainerFromCourse() throws Exception {
        // First assign trainer
        testCourse.setTrainer(testTrainer);
        testCourse = courseRepository.save(testCourse);

        // Perform DELETE request
        mockMvc
                .perform(delete("/api/courses/{courseId}/trainer", testCourse.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCourse.getId().intValue())))
                .andExpect(jsonPath("$.trainer").doesNotExist());

        // Verify trainer was removed in database
        Optional<Course> updatedCourse = courseRepository.findById(testCourse.getId());
        assertTrue(updatedCourse.isPresent());
        assertNull(updatedCourse.get().getTrainer());
    }

    @Test
    void changeCourseStatus_ShouldUpdateCourseStatus() throws Exception {
        // Assign trainer (needed for activating a course)
        testCourse.setTrainer(testTrainer);
        testCourse = courseRepository.save(testCourse);

        // Perform PUT request
        mockMvc
                .perform(put("/api/courses/{id}/status", testCourse.getId()).param("status",
                                                                                   CourseStatus.ACTIVE.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testCourse.getId().intValue())))
                .andExpect(jsonPath("$.status", is(CourseStatus.ACTIVE.toString())));

        // Verify status was updated in database
        Optional<Course> updatedCourse = courseRepository.findById(testCourse.getId());
        assertTrue(updatedCourse.isPresent());
        assertEquals(CourseStatus.ACTIVE, updatedCourse.get().getStatus());
    }

    @Test
    void createCourse_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Create invalid DTO (missing required fields)
        CourseDto invalidDto = new CourseDto();
        invalidDto.setDescription("This course is missing required fields");

        // Perform POST request
        mockMvc.perform(post("/api/courses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidDto))).andExpect(
                status().isBadRequest()).andExpect(jsonPath("$.status", is(400))).andExpect(
                jsonPath("$.fieldErrors", aMapWithSize(greaterThan(0))));
    }
}

@TestConfiguration
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                                                      classes = UserService.class))
class TestConfig {
    // Define your test beans here
} 
