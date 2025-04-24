package com.ag.fuzz_unit_test.fuzz_unit_test.service;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.*;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.BookingRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ParticipantService participantService;

    private Participant participant;
    private Course course;
    private Booking booking;

    @BeforeEach
    void setUp() {
        // Initialize test participant
        participant = new Participant();
        participant.setId(1L);
        participant.setFirstName("John");
        participant.setLastName("Doe");
        participant.setEmail("john.doe@example.com");
        participant.setStatus(ParticipantStatus.ACTIVE);
        participant.setBookings(new ArrayList<>());

        // Initialize test course
        course = new Course();
        course.setId(1L);
        course.setName("Java Programming");
        course.setStartDate(LocalDate.now().plusDays(10));
        course.setEndDate(LocalDate.now().plusDays(20));
        course.setStatus(CourseStatus.PLANNED);
        course.setMaxSeats(10);
        course.setBookings(new ArrayList<>());

        // Initialize test booking
        booking = new Booking();
        booking.setId(1L);
        booking.setParticipant(participant);
        booking.setCourse(course);
        booking.setStatus(BookingStatus.PENDING);
        booking.setBookingDate(LocalDate.now());
    }

    @Test
    @DisplayName("Should create a participant successfully")
    void createParticipant_Success() {
        // Given
        when(participantRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        // When
        Participant createdParticipant = participantService.createParticipant(participant);

        // Then
        assertNotNull(createdParticipant);
        assertEquals(participant.getEmail(), createdParticipant.getEmail());
        verify(participantRepository, times(1)).findByEmail(participant.getEmail());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    @DisplayName("Should throw BusinessException when participant email already exists")
    void createParticipant_EmailExists() {
        // Given
        when(participantRepository.findByEmail(anyString())).thenReturn(Optional.of(participant));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.createParticipant(participant);
        });

        assertEquals("A participant with email " + participant.getEmail() + " already exists", exception.getMessage());
        verify(participantRepository, times(1)).findByEmail(participant.getEmail());
        verify(participantRepository, never()).save(any(Participant.class));
    }

    @Test
    @DisplayName("Should set ACTIVE status when creating participant without status")
    void createParticipant_DefaultStatus() {
        // Given
        participant.setStatus(null);
        when(participantRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        // When
        participantService.createParticipant(participant);

        // Then
        assertEquals(ParticipantStatus.ACTIVE, participant.getStatus());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    @DisplayName("Should book a course successfully")
    void bookCourse_Success() {
        // Given
        when(participantRepository.findById(anyLong())).thenReturn(Optional.of(participant));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            savedBooking.setId(1L);
            return savedBooking;
        });

        // When
        Booking result = participantService.bookCourse(participant.getId(), course.getId());

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(participant, result.getParticipant());
        assertEquals(course, result.getCourse());
        
        // Verify interactions
        verify(participantRepository, times(1)).findById(participant.getId());
        verify(courseRepository, times(1)).findById(course.getId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when participant not found")
    void bookCourse_ParticipantNotFound() {
        // Given
        when(participantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            participantService.bookCourse(99L, course.getId());
        });

        assertEquals("Participant not found with id: 99", exception.getMessage());
        verify(participantRepository, times(1)).findById(99L);
        verify(courseRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when course not found")
    void bookCourse_CourseNotFound() {
        // Given
        when(participantRepository.findById(anyLong())).thenReturn(Optional.of(participant));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            participantService.bookCourse(participant.getId(), 99L);
        });

        assertEquals("Course not found with id: 99", exception.getMessage());
        verify(participantRepository, times(1)).findById(participant.getId());
        verify(courseRepository, times(1)).findById(99L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when participant is not active")
    void bookCourse_ParticipantNotActive() {
        // Given
        participant.setStatus(ParticipantStatus.INACTIVE);
        when(participantRepository.findById(anyLong())).thenReturn(Optional.of(participant));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.bookCourse(participant.getId(), course.getId());
        });

        assertEquals("Participant is not active and cannot book courses", exception.getMessage());
        verify(participantRepository, times(1)).findById(participant.getId());
        verify(courseRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when course is not in a bookable state")
    void bookCourse_CourseNotBookable() {
        // Given
        course.setStatus(CourseStatus.COMPLETED);
        when(participantRepository.findById(anyLong())).thenReturn(Optional.of(participant));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.bookCourse(participant.getId(), course.getId());
        });

        assertEquals("Course is not available for booking", exception.getMessage());
        verify(participantRepository, times(1)).findById(participant.getId());
        verify(courseRepository, times(1)).findById(course.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when participant already has a booking for this course")
    void bookCourse_AlreadyBooked() {
        // Given
        booking.setParticipant(participant);
        booking.setCourse(course);
        participant.getBookings().add(booking);
        course.getBookings().add(booking);

        when(participantRepository.findById(anyLong())).thenReturn(Optional.of(participant));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.bookCourse(participant.getId(), course.getId());
        });

        assertEquals("Participant already has a booking for this course", exception.getMessage());
        verify(participantRepository, times(1)).findById(participant.getId());
        verify(courseRepository, times(1)).findById(course.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when course is fully booked")
    void bookCourse_CourseFull() {
        // Given
        // Create confirmed bookings to fill the course
        List<Booking> confirmedBookings = new ArrayList<>();
        for (int i = 0; i < course.getMaxSeats(); i++) {
            Participant otherParticipant = new Participant();
            otherParticipant.setId((long) (i + 2));
            
            Booking confirmedBooking = new Booking();
            confirmedBooking.setId((long) (i + 2));
            confirmedBooking.setParticipant(otherParticipant);
            confirmedBooking.setCourse(course);
            confirmedBooking.setStatus(BookingStatus.CONFIRMED);
            confirmedBookings.add(confirmedBooking);
        }
        course.setBookings(confirmedBookings);

        when(participantRepository.findById(anyLong())).thenReturn(Optional.of(participant));
        when(courseRepository.findById(anyLong())).thenReturn(Optional.of(course));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.bookCourse(participant.getId(), course.getId());
        });

        assertEquals("Course is fully booked", exception.getMessage());
        verify(participantRepository, times(1)).findById(participant.getId());
        verify(courseRepository, times(1)).findById(course.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should cancel a booking successfully")
    void cancelBooking_Success() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        
        // When
        Booking result = participantService.cancelBooking(booking.getId());

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Should throw BusinessException when trying to cancel a completed booking")
    void cancelBooking_CompletedBooking() {
        // Given
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.cancelBooking(booking.getId());
        });

        assertEquals("Cannot cancel a completed booking", exception.getMessage());
        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when trying to cancel an already cancelled booking")
    void cancelBooking_AlreadyCancelled() {
        // Given
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.cancelBooking(booking.getId());
        });

        assertEquals("Booking is already cancelled", exception.getMessage());
        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should change booking status successfully")
    void changeBookingStatus_Success() {
        // Setup a properly configured booking
        Course course = new Course();
        course.setId(1L);
        course.setMaxSeats(10);
        
        Participant participant = new Participant();
        participant.setId(1L);
        
        booking = new Booking();
        booking.setId(1L);
        booking.setCourse(course);
        booking.setParticipant(participant);
        booking.setStatus(BookingStatus.PENDING);
        
        // Setup the course with an empty bookings list for capacity check
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        course.setBookings(bookings);
        
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        
        // Mock save to return the modified booking
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            return savedBooking;
        });
        
        // When
        Booking result = participantService.changeBookingStatus(booking.getId(), BookingStatus.CONFIRMED);

        // Then
        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when trying to make an invalid booking status transition")
    void changeBookingStatus_InvalidTransition() {
        // Given
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.changeBookingStatus(booking.getId(), BookingStatus.CONFIRMED);
        });

        assertTrue(exception.getMessage().contains("Cannot change status of a cancelled booking"));
        verify(bookingRepository, times(1)).findById(booking.getId());
        verify(bookingRepository, never()).save(any(Booking.class));
    }
} 