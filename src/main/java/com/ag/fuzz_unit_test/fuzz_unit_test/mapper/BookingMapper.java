package com.ag.fuzz_unit_test.fuzz_unit_test.mapper;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.BookingDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.BookingSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    private final ParticipantMapper participantMapper;
    private final CourseMapper courseMapper;

    @Autowired
    public BookingMapper(@Lazy ParticipantMapper participantMapper, @Lazy CourseMapper courseMapper) {
        this.participantMapper = participantMapper;
        this.courseMapper = courseMapper;
    }

    public BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStatus(booking.getStatus());

        // Set participant
        if (booking.getParticipant() != null) {
            dto.setParticipant(participantMapper.toSummaryDto(booking.getParticipant()));
        }

        // Set course
        if (booking.getCourse() != null) {
            dto.setCourse(courseMapper.toSummaryDto(booking.getCourse()));
        }

        return dto;
    }

    public Booking toEntity(BookingDto dto) {
        if (dto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setBookingDate(dto.getBookingDate());
        booking.setStatus(dto.getStatus());

        return booking;
    }

    public BookingSummaryDto toSummaryDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingSummaryDto dto = new BookingSummaryDto();
        dto.setId(booking.getId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStatus(booking.getStatus());

        return dto;
    }

    public List<BookingDto> toDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 