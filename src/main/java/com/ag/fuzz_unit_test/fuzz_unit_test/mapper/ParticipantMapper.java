package com.ag.fuzz_unit_test.fuzz_unit_test.mapper;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.BookingSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.ParticipantDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.ParticipantSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Participant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParticipantMapper {

    private final BookingMapper bookingMapper;

    public ParticipantMapper(BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    public ParticipantDto toDto(Participant participant) {
        if (participant == null) {
            return null;
        }

        ParticipantDto dto = new ParticipantDto();
        dto.setId(participant.getId());
        dto.setFirstName(participant.getFirstName());
        dto.setLastName(participant.getLastName());
        dto.setEmail(participant.getEmail());
        dto.setPhone(participant.getPhone());
        dto.setStatus(participant.getStatus());
        dto.setCreatedAt(participant.getCreatedAt());
        dto.setUpdatedAt(participant.getUpdatedAt());

        // Convert bookings to DTOs
        if (participant.getBookings() != null) {
            List<BookingSummaryDto> bookings = participant.getBookings().stream()
                    .map(bookingMapper::toSummaryDto)
                    .collect(Collectors.toList());
            dto.setBookings(bookings);
        }

        return dto;
    }

    public Participant toEntity(ParticipantDto dto) {
        if (dto == null) {
            return null;
        }

        Participant participant = new Participant();
        participant.setId(dto.getId());
        participant.setFirstName(dto.getFirstName());
        participant.setLastName(dto.getLastName());
        participant.setEmail(dto.getEmail());
        participant.setPhone(dto.getPhone());
        participant.setStatus(dto.getStatus());

        return participant;
    }

    public ParticipantSummaryDto toSummaryDto(Participant participant) {
        if (participant == null) {
            return null;
        }

        ParticipantSummaryDto dto = new ParticipantSummaryDto();
        dto.setId(participant.getId());
        dto.setFirstName(participant.getFirstName());
        dto.setLastName(participant.getLastName());
        dto.setEmail(participant.getEmail());
        dto.setStatus(participant.getStatus());

        return dto;
    }

    public List<ParticipantDto> toDtoList(List<Participant> participants) {
        return participants.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 