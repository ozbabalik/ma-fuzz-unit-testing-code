package com.ag.fuzz_unit_test.fuzz_unit_test.dto;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDto {
    private Long id;
    
    private LocalDate bookingDate;
    
    @NotNull(message = "Booking status is required")
    private BookingStatus status;
    
    private ParticipantSummaryDto participant;
    
    private CourseSummaryDto course;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public ParticipantSummaryDto getParticipant() {
        return participant;
    }

    public void setParticipant(ParticipantSummaryDto participant) {
        this.participant = participant;
    }

    public CourseSummaryDto getCourse() {
        return course;
    }

    public void setCourse(CourseSummaryDto course) {
        this.course = course;
    }
} 