package com.ag.fuzz_unit_test.fuzz_unit_test.unit;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.*;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.BookingRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.ParticipantRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

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
        // Setup participant
        participant = new Participant();
        participant.setId(1L);
        participant.setFirstName("John");
        participant.setLastName("Doe");
        participant.setEmail("john.doe@example.com");
        participant.setPhone("123-456-7890");
        participant.setStatus(ParticipantStatus.ACTIVE);
        participant.setBookings(new ArrayList<>());
        
        // Setup course
        course = new Course();
        course.setId(1L);
        course.setName("Java Programming");
        course.setMaxSeats(20);
        course.setStatus(CourseStatus.PLANNED);
        course.setBookings(new ArrayList<>());
        
        // Setup booking
        booking = new Booking();
        booking.setId(1L);
        booking.setParticipant(participant);
        booking.setCourse(course);
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(BookingStatus.PENDING);
    }

    @Test
    void createParticipant_WithUniqueEmail_ShouldCreateParticipant() {
        // Arrange
        when(participantRepository.findByEmail(participant.getEmail())).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);
        
        // Act
        Participant result = participantService.createParticipant(participant);
        
        // Assert
        assertNotNull(result);
        assertEquals(participant.getId(), result.getId());
        assertEquals(participant.getEmail(), result.getEmail());
        verify(participantRepository).findByEmail(participant.getEmail());
        verify(participantRepository).save(participant);
    }
    
    @Test
    void createParticipant_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(participantRepository.findByEmail(participant.getEmail())).thenReturn(Optional.of(participant));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> participantService.createParticipant(participant));
        verify(participantRepository).findByEmail(participant.getEmail());
        verify(participantRepository, never()).save(any(Participant.class));
    }
    
    @Test
    void createParticipant_WithNullStatus_ShouldSetDefaultStatus() {
        // Arrange
        participant.setStatus(null);
        when(participantRepository.findByEmail(participant.getEmail())).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> {
            Participant saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        
        // Act
        Participant result = participantService.createParticipant(participant);
        
        // Assert
        assertEquals(ParticipantStatus.ACTIVE, result.getStatus());
    }

    @Test
    void updateParticipant_WhenParticipantExists_ShouldUpdateParticipant() {
        // Arrange
        Participant updatedDetails = new Participant();
        updatedDetails.setFirstName("John Updated");
        updatedDetails.setLastName("Doe Updated");
        updatedDetails.setEmail("john.updated@example.com");
        updatedDetails.setPhone("987-654-3210");
        updatedDetails.setStatus(ParticipantStatus.INACTIVE);
        
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(participantRepository.findByEmail(updatedDetails.getEmail())).thenReturn(Optional.empty());
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Participant result = participantService.updateParticipant(1L, updatedDetails);
        
        // Assert
        assertEquals(updatedDetails.getFirstName(), result.getFirstName());
        assertEquals(updatedDetails.getLastName(), result.getLastName());
        assertEquals(updatedDetails.getEmail(), result.getEmail());
        assertEquals(updatedDetails.getPhone(), result.getPhone());
        assertEquals(updatedDetails.getStatus(), result.getStatus());
    }
    
    @Test
    void updateParticipant_WithExistingEmail_ShouldThrowException() {
        // Arrange
        Participant updatedDetails = new Participant();
        updatedDetails.setEmail("existing@example.com");
        
        Participant existingParticipant = new Participant();
        existingParticipant.setId(2L);
        existingParticipant.setEmail("existing@example.com");
        
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(participantRepository.findByEmail(updatedDetails.getEmail())).thenReturn(Optional.of(existingParticipant));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> participantService.updateParticipant(1L, updatedDetails));
    }

    @Test
    void bookCourse_WhenParticipantAndCourseExist_ShouldCreateBooking() {
        // Arrange
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        
        // Act
        Booking result = participantService.bookCourse(1L, 1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(participant, result.getParticipant());
        assertEquals(course, result.getCourse());
        assertEquals(BookingStatus.PENDING, result.getStatus());
    }
    
    @Test
    void bookCourse_WhenParticipantNotActive_ShouldThrowException() {
        // Arrange
        participant.setStatus(ParticipantStatus.INACTIVE);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> participantService.bookCourse(1L, 1L));
        verify(courseRepository, never()).findById(anyLong());
    }
    
    @Test
    void bookCourse_WhenCourseNotBookable_ShouldThrowException() {
        // Arrange
        course.setStatus(CourseStatus.COMPLETED);
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> participantService.bookCourse(1L, 1L));
    }
    
    @Test
    void bookCourse_WhenParticipantAlreadyBooked_ShouldThrowException() {
        // Arrange
        participant.getBookings().add(booking);
        
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> participantService.bookCourse(1L, 1L));
    }
    
    @Test
    void bookCourse_WhenCourseFullyBooked_ShouldThrowException() {
        // Arrange
        course.setMaxSeats(1);
        Booking existingBooking = new Booking();
        existingBooking.setId(2L);
        existingBooking.setStatus(BookingStatus.CONFIRMED);
        course.getBookings().add(existingBooking);
        
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> participantService.bookCourse(1L, 1L));
    }

    @Test
    void cancelBooking_WhenBookingExists_ShouldCancelBooking() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Booking result = participantService.cancelBooking(1L);
        
        // Assert
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }
    
    @Test
    void cancelBooking_WhenBookingCompleted_ShouldThrowException() {
        // Arrange
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> participantService.cancelBooking(1L));
    }
    
    @Test
    void cancelBooking_WhenBookingAlreadyCancelled_ShouldThrowException() {
        // Arrange
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        
        // Act & Assert
        assertThrows(BusinessException.class, () -> participantService.cancelBooking(1L));
    }

    @Test
    void changeBookingStatus_WhenValidTransition_ShouldChangeStatus() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Booking result = participantService.changeBookingStatus(1L, BookingStatus.CONFIRMED);
        
        // Assert
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }
    
    @Test
    void changeBookingStatus_WhenInvalidTransition_ShouldThrowException() {
        // Arrange
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        
        // Act & Assert
        assertThrows(BusinessException.class, 
                () -> participantService.changeBookingStatus(1L, BookingStatus.COMPLETED));
    }
    
    @Test
    void changeBookingStatus_WhenConfirmingFullCourse_ShouldThrowException() {
        // Arrange
        course.setMaxSeats(1);
        Booking existingBooking = new Booking();
        existingBooking.setId(2L);
        existingBooking.setStatus(BookingStatus.CONFIRMED);
        course.getBookings().add(existingBooking);
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        
        // Act & Assert
        assertThrows(BusinessException.class, 
                () -> participantService.changeBookingStatus(1L, BookingStatus.CONFIRMED));
    }

    @Test
    void getAllParticipants_ShouldReturnAllParticipants() {
        // Arrange
        List<Participant> participants = Arrays.asList(participant, new Participant());
        when(participantRepository.findAll()).thenReturn(participants);
        
        // Act
        List<Participant> result = participantService.getAllParticipants();
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(participant));
    }

    @Test
    void getParticipantById_WhenParticipantExists_ShouldReturnParticipant() {
        // Arrange
        when(participantRepository.findById(1L)).thenReturn(Optional.of(participant));
        
        // Act
        Participant result = participantService.getParticipantById(1L);
        
        // Assert
        assertEquals(participant.getId(), result.getId());
        assertEquals(participant.getEmail(), result.getEmail());
    }
    
    @Test
    void getParticipantById_WhenParticipantDoesNotExist_ShouldThrowException() {
        // Arrange
        when(participantRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> participantService.getParticipantById(99L));
    }

    @Test
    void getParticipantByEmail_WhenParticipantExists_ShouldReturnParticipant() {
        // Arrange
        when(participantRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(participant));
        
        // Act
        Participant result = participantService.getParticipantByEmail("john.doe@example.com");
        
        // Assert
        assertEquals(participant.getId(), result.getId());
        assertEquals(participant.getEmail(), result.getEmail());
    }
    
    @Test
    void getParticipantByEmail_WhenParticipantDoesNotExist_ShouldThrowException() {
        // Arrange
        when(participantRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> participantService.getParticipantByEmail("nonexistent@example.com"));
    }
} 