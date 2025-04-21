package com.ag.fuzz_unit_test.fuzz_unit_test.dto;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.BookingStatus;

import java.time.LocalDate;

public class BookingSummaryDto {
    private Long id;
    private LocalDate bookingDate;
    private BookingStatus status;

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
} 