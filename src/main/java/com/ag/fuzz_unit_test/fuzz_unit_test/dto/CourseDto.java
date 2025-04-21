package com.ag.fuzz_unit_test.fuzz_unit_test.dto;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDto {
    private Long id;

    @NotBlank(message = "Course name is required")
    private String name;

    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private CourseStatus status;

    @NotNull(message = "Maximum seats is required")
    @Min(value = 1, message = "Maximum seats must be at least 1")
    private Integer maxSeats;

    private TrainerSummaryDto trainer;
    
    private List<BookingSummaryDto> bookings;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public Integer getMaxSeats() {
        return maxSeats;
    }

    public void setMaxSeats(Integer maxSeats) {
        this.maxSeats = maxSeats;
    }

    public TrainerSummaryDto getTrainer() {
        return trainer;
    }

    public void setTrainer(TrainerSummaryDto trainer) {
        this.trainer = trainer;
    }

    public List<BookingSummaryDto> getBookings() {
        return bookings;
    }

    public void setBookings(List<BookingSummaryDto> bookings) {
        this.bookings = bookings;
    }
} 