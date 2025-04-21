package com.ag.fuzz_unit_test.fuzz_unit_test.fuzz;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.*;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.BookingRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.ParticipantRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.ParticipantService;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class BookingServiceFuzzTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ParticipantService participantService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup default mocks
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(1L);
            }
            return b;
        });
    }

    @FuzzTest
    void fuzzBookCourse(FuzzedDataProvider data) {
        // Generate random conditions
        boolean participantExists = data.consumeBoolean();
        boolean courseExists = data.consumeBoolean();
        ParticipantStatus participantStatus = Arrays.asList(ParticipantStatus.values())
                .get(data.consumeInt(0, ParticipantStatus.values().length - 1));
        CourseStatus courseStatus = Arrays.asList(CourseStatus.values())
                .get(data.consumeInt(0, CourseStatus.values().length - 1));
        boolean hasExistingBooking = data.consumeBoolean();
        boolean isCourseFull = data.consumeBoolean();

        // Generate IDs
        long participantId = data.consumeLong(1, 1000);
        long courseId = data.consumeLong(1, 1000);

        // Create participant
        Participant participant = new Participant();
        participant.setId(participantId);
        participant.setFirstName("Test");
        participant.setLastName("Participant");
        participant.setEmail("participant@example.com");
        participant.setStatus(participantStatus);
        participant.setBookings(new ArrayList<>());
        
        // Create course
        Course course = new Course();
        course.setId(courseId);
        course.setName("Test Course");
        course.setStartDate(LocalDate.now().plusDays(10));
        course.setEndDate(LocalDate.now().plusDays(20));
        course.setMaxSeats(10);
        course.setStatus(courseStatus);
        course.setBookings(new ArrayList<>());

        // Setup existing booking if needed
        if (hasExistingBooking) {
            Booking existingBooking = new Booking();
            existingBooking.setId(1L);
            existingBooking.setParticipant(participant);
            existingBooking.setCourse(course);
            existingBooking.setStatus(BookingStatus.CONFIRMED);
            existingBooking.setBookingDate(LocalDate.now());
            participant.getBookings().add(existingBooking);
            
            when(bookingRepository.findByParticipantIdAndCourseId(participantId, courseId))
                    .thenReturn(Optional.of(existingBooking));
        } else {
            when(bookingRepository.findByParticipantIdAndCourseId(participantId, courseId))
                    .thenReturn(Optional.empty());
        }

        // Setup course to be full if needed
        if (isCourseFull) {
            List<Booking> confirmedBookings = IntStream.range(0, course.getMaxSeats())
                    .mapToObj(i -> {
                        Booking b = new Booking();
                        b.setId((long) (i + 10));
                        b.setCourse(course);
                        b.setStatus(BookingStatus.CONFIRMED);
                        return b;
                    })
                    .collect(Collectors.toList());
            course.setBookings(confirmedBookings);
        }

        // Setup mocks
        if (participantExists) {
            when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        } else {
            when(participantRepository.findById(participantId)).thenReturn(Optional.empty());
        }
        
        if (courseExists) {
            when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        } else {
            when(courseRepository.findById(courseId)).thenReturn(Optional.empty());
        }

        try {
            // Attempt to book the course
            participantService.bookCourse(participantId, courseId);
        } catch (BusinessException e) {
            // Expected in many scenarios: inactive participant, fully booked course, etc.
        } catch (Exception e) {
            // ResourceNotFoundException is expected if participant or course doesn't exist
            if (!participantExists || !courseExists) {
                if (!(e.getMessage() != null && e.getMessage().contains("not found"))) {
                    throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
                }
            } else {
                throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
            }
        }
    }

    @FuzzTest
    void fuzzChangeBookingStatus(FuzzedDataProvider data) {
        // Generate random conditions
        boolean bookingExists = data.consumeBoolean();
        BookingStatus currentStatus = Arrays.asList(BookingStatus.values())
                .get(data.consumeInt(0, BookingStatus.values().length - 1));
        BookingStatus newStatus = Arrays.asList(BookingStatus.values())
                .get(data.consumeInt(0, BookingStatus.values().length - 1));
        
        // Create booking
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(currentStatus);
        booking.setBookingDate(LocalDate.now());
        
        Participant participant = new Participant();
        participant.setId(1L);
        booking.setParticipant(participant);
        
        Course course = new Course();
        course.setId(1L);
        booking.setCourse(course);

        // Setup mocks
        if (bookingExists) {
            when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        } else {
            when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        }

        try {
            // Attempt to change booking status
            participantService.changeBookingStatus(1L, newStatus);
        } catch (BusinessException e) {
            // Expected in many scenarios: invalid status transition
        } catch (Exception e) {
            // ResourceNotFoundException is expected if booking doesn't exist
            if (!bookingExists) {
                if (!(e.getMessage() != null && e.getMessage().contains("not found"))) {
                    throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
                }
            } else {
                throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
            }
        }
    }
} 