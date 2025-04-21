package com.ag.fuzz_unit_test.fuzz_unit_test.controller;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.BookingDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.ParticipantDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Booking;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Participant;
import com.ag.fuzz_unit_test.fuzz_unit_test.mapper.BookingMapper;
import com.ag.fuzz_unit_test.fuzz_unit_test.mapper.ParticipantMapper;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.ParticipantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantService participantService;
    private final ParticipantMapper participantMapper;
    private final BookingMapper bookingMapper;

    @Autowired
    public ParticipantController(ParticipantService participantService, 
                                ParticipantMapper participantMapper,
                                BookingMapper bookingMapper) {
        this.participantService = participantService;
        this.participantMapper = participantMapper;
        this.bookingMapper = bookingMapper;
    }

    @GetMapping
    public ResponseEntity<List<ParticipantDto>> getAllParticipants() {
        List<Participant> participants = participantService.getAllParticipants();
        return ResponseEntity.ok(participantMapper.toDtoList(participants));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDto> getParticipantById(@PathVariable Long id) {
        Participant participant = participantService.getParticipantById(id);
        return ResponseEntity.ok(participantMapper.toDto(participant));
    }

    @PostMapping
    public ResponseEntity<ParticipantDto> createParticipant(@Valid @RequestBody ParticipantDto participantDto) {
        Participant participant = participantMapper.toEntity(participantDto);
        Participant createdParticipant = participantService.createParticipant(participant);
        return new ResponseEntity<>(participantMapper.toDto(createdParticipant), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParticipantDto> updateParticipant(@PathVariable Long id, 
                                                          @Valid @RequestBody ParticipantDto participantDto) {
        Participant participant = participantMapper.toEntity(participantDto);
        Participant updatedParticipant = participantService.updateParticipant(id, participant);
        return ResponseEntity.ok(participantMapper.toDto(updatedParticipant));
    }

    @PostMapping("/{id}/bookings")
    public ResponseEntity<BookingDto> bookCourse(@PathVariable Long id, @RequestParam Long courseId) {
        Booking booking = participantService.bookCourse(id, courseId);
        return new ResponseEntity<>(bookingMapper.toDto(booking), HttpStatus.CREATED);
    }
} 