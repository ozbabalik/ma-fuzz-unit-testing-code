package com.ag.fuzz_unit_test.fuzz_unit_test.service;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.*;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.ResourceNotFoundException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.BookingRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.ParticipantRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final CourseRepository courseRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public ParticipantService(ParticipantRepository participantRepository,
                              CourseRepository courseRepository,
                              BookingRepository bookingRepository) {
        this.participantRepository = participantRepository;
        this.courseRepository = courseRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Create a new participant
     *
     * @param participant the participant to create
     * @return the created participant with ID
     */
    @Transactional
    public Participant createParticipant(@Valid @NotNull Participant participant) {
        // Check if email already exists
        Optional<Participant> existingParticipant = participantRepository.findByEmail(participant.getEmail());
        if (existingParticipant.isPresent()) {
            throw new BusinessException("A participant with email " + participant.getEmail() + " already exists");
        }

        // Set default status if not provided
        if (participant.getStatus() == null) {
            participant.setStatus(ParticipantStatus.ACTIVE);
        }

        return participantRepository.save(participant);
    }

    /**
     * Update an existing participant
     *
     * @param id the ID of the participant to update
     * @param participantDetails the updated participant details
     * @return the updated participant
     */
    @Transactional
    public Participant updateParticipant(Long id, @Valid @NotNull Participant participantDetails) {
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + id));

        // Check if email is being changed and already exists
        if (!participant.getEmail().equals(participantDetails.getEmail())) {
            Optional<Participant> existingParticipant = participantRepository.findByEmail(participantDetails.getEmail());
            if (existingParticipant.isPresent()) {
                throw new BusinessException("A participant with email " + participantDetails.getEmail() + " already exists");
            }
        }

        participant.setFirstName(participantDetails.getFirstName());
        participant.setLastName(participantDetails.getLastName());
        participant.setEmail(participantDetails.getEmail());
        participant.setPhone(participantDetails.getPhone());
        participant.setStatus(participantDetails.getStatus());

        return participantRepository.save(participant);
    }

    /**
     * Book a course for a participant
     *
     * @param participantId the ID of the participant
     * @param courseId the ID of the course to book
     * @return the created booking
     */
    @Transactional
    public Booking bookCourse(Long participantId, Long courseId) {
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + participantId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        // Check if participant is active
        if (participant.getStatus() != ParticipantStatus.ACTIVE) {
            throw new BusinessException("Participant is not active and cannot book courses");
        }

        // Check if course is in a bookable state
        if (course.getStatus() != CourseStatus.PLANNED && course.getStatus() != CourseStatus.ACTIVE) {
            throw new BusinessException("Course is not available for booking");
        }

        // Check if participant already has a booking for this course
        boolean hasExistingBooking = participant.getBookings().stream()
                .anyMatch(b -> b.getCourse().getId().equals(courseId) && 
                        b.getStatus() != BookingStatus.CANCELLED);
        
        if (hasExistingBooking) {
            throw new BusinessException("Participant already has a booking for this course");
        }

        // Check if course has available seats
        long confirmedBookingsCount = course.getBookings().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING)
                .count();
        
        if (confirmedBookingsCount >= course.getMaxSeats()) {
            throw new BusinessException("Course is fully booked");
        }

        // Create new booking
        Booking booking = new Booking();
        booking.setParticipant(participant);
        booking.setCourse(course);
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    /**
     * Cancel a booking
     *
     * @param bookingId the ID of the booking to cancel
     * @return the updated booking
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Validate booking status transition
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed booking");
        }
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled");
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    /**
     * Change the status of a booking
     *
     * @param bookingId the ID of the booking
     * @param newStatus the new status to set
     * @return the updated booking
     */
    @Transactional
    public Booking changeBookingStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Validate booking status transition
        validateBookingStatusTransition(booking.getStatus(), newStatus);

        // If confirming a booking, check course capacity again
        if (newStatus == BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.CONFIRMED) {
            Course course = booking.getCourse();
            
            long confirmedBookingsCount = course.getBookings().stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED && !b.getId().equals(bookingId))
                    .count();
            
            if (confirmedBookingsCount >= course.getMaxSeats()) {
                throw new BusinessException("Cannot confirm booking: course is fully booked");
            }
        }

        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    /**
     * Get all participants
     *
     * @return list of all participants
     */
    @Transactional(readOnly = true)
    public List<Participant> getAllParticipants() {
        return participantRepository.findAll();
    }

    /**
     * Get participant by ID
     *
     * @param id the participant ID
     * @return the participant
     */
    @Transactional(readOnly = true)
    public Participant getParticipantById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + id));
    }

    /**
     * Get participant by email
     *
     * @param email the participant email
     * @return the participant
     */
    @Transactional(readOnly = true)
    public Participant getParticipantByEmail(String email) {
        return participantRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with email: " + email));
    }

    /**
     * Validate booking status transitions
     *
     * @param currentStatus the current booking status
     * @param newStatus the new booking status
     */
    private void validateBookingStatusTransition(BookingStatus currentStatus, BookingStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // No change, always valid
        }

        switch (currentStatus) {
            case PENDING:
                // From PENDING, can move to CONFIRMED or CANCELLED
                if (newStatus != BookingStatus.CONFIRMED && newStatus != BookingStatus.CANCELLED) {
                    throw new BusinessException("Invalid booking status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case CONFIRMED:
                // From CONFIRMED, can move to COMPLETED or CANCELLED
                if (newStatus != BookingStatus.COMPLETED && newStatus != BookingStatus.CANCELLED) {
                    throw new BusinessException("Invalid booking status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case CANCELLED:
                // From CANCELLED, can't change status
                throw new BusinessException("Cannot change status of a cancelled booking");
            case COMPLETED:
                // From COMPLETED, can't change status
                throw new BusinessException("Cannot change status of a completed booking");
        }
    }
} 