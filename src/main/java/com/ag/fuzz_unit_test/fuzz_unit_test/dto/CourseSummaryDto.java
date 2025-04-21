package com.ag.fuzz_unit_test.fuzz_unit_test.dto;

import com.ag.fuzz_unit_test.fuzz_unit_test.entity.CourseStatus;

import java.time.LocalDate;

public class CourseSummaryDto {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private CourseStatus status;
    private Integer maxSeats;

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
} 