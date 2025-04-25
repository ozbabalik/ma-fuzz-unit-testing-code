package com.ag.fuzz_unit_test.fuzz_unit_test.integration;

import com.ag.fuzz_unit_test.fuzz_unit_test.controller.BookingController;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.BookingDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.CourseSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.ParticipantSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Booking;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.BookingStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Participant;
import com.ag.fuzz_unit_test.fuzz_unit_test.mapper.BookingMapper;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParticipantService participantService;

    @MockBean
    private BookingMapper bookingMapper;

    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        // Setup participant
        Participant participant = new Participant();
        participant.setId(1L);
        participant.setFirstName("John");
        participant.setLastName("Doe");
        participant.setEmail("john.doe@example.com");

        // Setup course
        Course course = new Course();
        course.setId(1L);
        course.setName("Java Programming");

        // Setup booking
        booking = new Booking();
        booking.setId(1L);
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(BookingStatus.PENDING);
        booking.setParticipant(participant);
        booking.setCourse(course);

        // Setup participant summary DTO
        ParticipantSummaryDto participantSummaryDto = new ParticipantSummaryDto();
        participantSummaryDto.setId(1L);
        participantSummaryDto.setFirstName("John");
        participantSummaryDto.setLastName("Doe");
        participantSummaryDto.setEmail("john.doe@example.com");

        // Setup course summary DTO
        CourseSummaryDto courseSummaryDto = new CourseSummaryDto();
        courseSummaryDto.setId(1L);
        courseSummaryDto.setName("Java Programming");

        // Setup booking DTO
        bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setBookingDate(LocalDate.now());
        bookingDto.setStatus(BookingStatus.PENDING);
        bookingDto.setParticipant(participantSummaryDto);
        bookingDto.setCourse(courseSummaryDto);
    }

    @Test
    void changeBookingStatus_ShouldReturnUpdatedBooking() throws Exception {
        // Setup updated booking
        Booking updatedBooking = new Booking();
        updatedBooking.setId(1L);
        updatedBooking.setBookingDate(booking.getBookingDate());
        updatedBooking.setStatus(BookingStatus.CONFIRMED);
        updatedBooking.setParticipant(booking.getParticipant());
        updatedBooking.setCourse(booking.getCourse());

        // Setup updated DTO
        BookingDto updatedDto = new BookingDto();
        updatedDto.setId(1L);
        updatedDto.setBookingDate(bookingDto.getBookingDate());
        updatedDto.setStatus(BookingStatus.CONFIRMED);
        updatedDto.setParticipant(bookingDto.getParticipant());
        updatedDto.setCourse(bookingDto.getCourse());

        // Mock service and mapper
        when(participantService.changeBookingStatus(1L, BookingStatus.CONFIRMED)).thenReturn(updatedBooking);
        when(bookingMapper.toDto(updatedBooking)).thenReturn(updatedDto);

        // Perform request and verify
        mockMvc.perform(put("/api/bookings/1/status")
                .param("status", "CONFIRMED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        verify(participantService).changeBookingStatus(1L, BookingStatus.CONFIRMED);
        verify(bookingMapper).toDto(updatedBooking);
    }

    @Test
    void cancelBooking_ShouldReturnCancelledBooking() throws Exception {
        // Setup cancelled booking
        Booking cancelledBooking = new Booking();
        cancelledBooking.setId(1L);
        cancelledBooking.setBookingDate(booking.getBookingDate());
        cancelledBooking.setStatus(BookingStatus.CANCELLED);
        cancelledBooking.setParticipant(booking.getParticipant());
        cancelledBooking.setCourse(booking.getCourse());

        // Setup cancelled DTO
        BookingDto cancelledDto = new BookingDto();
        cancelledDto.setId(1L);
        cancelledDto.setBookingDate(bookingDto.getBookingDate());
        cancelledDto.setStatus(BookingStatus.CANCELLED);
        cancelledDto.setParticipant(bookingDto.getParticipant());
        cancelledDto.setCourse(bookingDto.getCourse());

        // Mock service and mapper
        when(participantService.cancelBooking(1L)).thenReturn(cancelledBooking);
        when(bookingMapper.toDto(cancelledBooking)).thenReturn(cancelledDto);

        // Perform request and verify
        mockMvc.perform(put("/api/bookings/1/cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        verify(participantService).cancelBooking(1L);
        verify(bookingMapper).toDto(cancelledBooking);
    }
} 