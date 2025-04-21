package com.ag.fuzz_unit_test.fuzz_unit_test.controller;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.BookingDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Booking;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.BookingStatus;
import com.ag.fuzz_unit_test.fuzz_unit_test.mapper.BookingMapper;
import com.ag.fuzz_unit_test.fuzz_unit_test.service.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final ParticipantService participantService;
    private final BookingMapper bookingMapper;

    @Autowired
    public BookingController(ParticipantService participantService, BookingMapper bookingMapper) {
        this.participantService = participantService;
        this.bookingMapper = bookingMapper;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingDto> changeBookingStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        Booking booking = participantService.changeBookingStatus(id, status);
        return ResponseEntity.ok(bookingMapper.toDto(booking));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable Long id) {
        Booking booking = participantService.cancelBooking(id);
        return ResponseEntity.ok(bookingMapper.toDto(booking));
    }
} 