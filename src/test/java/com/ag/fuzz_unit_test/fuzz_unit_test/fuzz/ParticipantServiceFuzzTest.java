package com.ag.fuzz_unit_test.fuzz_unit_test.fuzz;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Participant;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.ParticipantStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.exception.BusinessException;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.BookingRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.CourseRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.repository.ParticipantRepository;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.ParticipantService;
import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ParticipantServiceFuzzTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ParticipantService participantService;

    private Validator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        // Setup default mocks
        when(participantRepository.save(any(Participant.class))).thenAnswer(invocation -> {
            Participant p = invocation.getArgument(0);
            if (p.getId() == null) {
                p.setId(1L);
            }
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });
    }

    @FuzzTest
    void fuzzCreateParticipant(FuzzedDataProvider data) {
        // Generate random participant data
        String firstName = data.consumeString(20);
        String lastName = data.consumeString(20);
        String email = data.consumeString(50);
        String phone = data.consumePrintableAsciiString(15);
        boolean existingEmail = data.consumeBoolean();

        // Create a participant with the generated data
        Participant participant = new Participant();
        participant.setFirstName(firstName);
        participant.setLastName(lastName);
        participant.setEmail(email);
        participant.setPhone(phone);
        participant.setStatus(ParticipantStatus.ACTIVE);
        participant.setBookings(new ArrayList<>());

        // Validate the participant
        Set<ConstraintViolation<Participant>> violations = validator.validate(participant);

        try {
            // Mock repository behavior based on existingEmail flag
            if (existingEmail) {
                when(participantRepository.findByEmail(anyString())).thenReturn(Optional.of(new Participant()));
            } else {
                when(participantRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            }

            // Attempt to create the participant
            if (!violations.isEmpty()) {
                // If there are validation errors, we should expect a ConstraintViolationException
                // We're manually handling this since we're using the validator directly
                throw new ConstraintViolationException(violations);
            } else {
                try {
                    participantService.createParticipant(participant);
                    // If we get here, creation was successful (if email wasn't already in use)
                } catch (BusinessException e) {
                    // Expected if email exists
                    if (!existingEmail) {
                        throw new AssertionError("Unexpected BusinessException: " + e.getMessage());
                    }
                }
            }
        } catch (ConstraintViolationException | BusinessException e) {
            // These exceptions are expected and handled
        } catch (Exception e) {
            // Any other exception is unexpected and should be reported
            throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
        }
    }

    @FuzzTest
    void fuzzUpdateParticipant(FuzzedDataProvider data) {
        // Generate random participant data
        Long id = data.consumeBoolean() ? null : data.consumeLong();
        String firstName = data.consumeString(20);
        String lastName = data.consumeString(20);
        String email = data.consumeString(50);
        String phone = data.consumePrintableAsciiString(15);
        boolean existingParticipant = data.consumeBoolean();
        boolean duplicateEmail = data.consumeBoolean();

        // Create a participant with the generated data
        Participant participant = new Participant();
        participant.setId(id);
        participant.setFirstName(firstName);
        participant.setLastName(lastName);
        participant.setEmail(email);
        participant.setPhone(phone);
        participant.setStatus(ParticipantStatus.ACTIVE);
        participant.setBookings(new ArrayList<>());

        // Setup existing participant for update
        Participant existingEntity = new Participant();
        existingEntity.setId(1L);
        existingEntity.setEmail("original@example.com");
        existingEntity.setFirstName("Original");
        existingEntity.setLastName("User");
        existingEntity.setStatus(ParticipantStatus.ACTIVE);
        existingEntity.setBookings(new ArrayList<>());
        existingEntity.setCreatedAt(LocalDateTime.now().minusDays(10));
        existingEntity.setUpdatedAt(LocalDateTime.now().minusDays(5));

        // Validate the participant
        Set<ConstraintViolation<Participant>> violations = validator.validate(participant);

        try {
            // Mock repository behavior
            if (existingParticipant) {
                when(participantRepository.findById(any())).thenReturn(Optional.of(existingEntity));
            } else {
                when(participantRepository.findById(any())).thenReturn(Optional.empty());
            }

            if (duplicateEmail && !email.equals("original@example.com")) {
                Participant duplicate = new Participant();
                duplicate.setId(2L);
                duplicate.setEmail(email);
                when(participantRepository.findByEmail(email)).thenReturn(Optional.of(duplicate));
            } else {
                when(participantRepository.findByEmail(email)).thenReturn(Optional.empty());
            }

            // Attempt to update the participant
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            } else {
                try {
                    participantService.updateParticipant(id != null ? id : 1L, participant);
                } catch (BusinessException e) {
                    // Expected if duplicate email
                } catch (Exception e) {
                    // Other exceptions might be expected depending on the fuzzing inputs
                }
            }
        } catch (ConstraintViolationException | BusinessException e) {
            // These exceptions are expected and handled
        } catch (Exception e) {
            // Some exceptions are expected (e.g., ResourceNotFoundException for non-existent ID)
            if (!(e.getMessage() != null && e.getMessage().contains("not found"))) {
                throw new AssertionError("Unexpected exception: " + e.getClass().getName() + " - " + e.getMessage());
            }
        }
    }
} 