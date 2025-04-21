package com.ag.fuzz_unit_test.fuzz_unit_test.mapper;

import com.ag.fuzz_unit_test.fuzz_unit_test.dto.BookingSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.CourseDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.dto.CourseSummaryDto;
import com.ag.fuzz_unit_test.fuzz_unit_test.entity.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseMapper {

    private final TrainerMapper trainerMapper;
    private BookingMapper bookingMapper;

    @Autowired
    public CourseMapper(@Lazy TrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Autowired
    public void setBookingMapper(@Lazy BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    public CourseDto toDto(Course course) {
        if (course == null) {
            return null;
        }

        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setStatus(course.getStatus());
        dto.setMaxSeats(course.getMaxSeats());

        // Set trainer
        if (course.getTrainer() != null) {
            dto.setTrainer(trainerMapper.toSummaryDto(course.getTrainer()));
        }

        // Convert bookings to DTOs
        if (course.getBookings() != null && bookingMapper != null) {
            List<BookingSummaryDto> bookings = course.getBookings().stream()
                    .map(bookingMapper::toSummaryDto)
                    .collect(Collectors.toList());
            dto.setBookings(bookings);
        }

        return dto;
    }

    public Course toEntity(CourseDto dto) {
        if (dto == null) {
            return null;
        }

        Course course = new Course();
        course.setId(dto.getId());
        course.setName(dto.getName());
        course.setDescription(dto.getDescription());
        course.setStartDate(dto.getStartDate());
        course.setEndDate(dto.getEndDate());
        course.setStatus(dto.getStatus());
        course.setMaxSeats(dto.getMaxSeats());

        return course;
    }

    public CourseSummaryDto toSummaryDto(Course course) {
        if (course == null) {
            return null;
        }

        CourseSummaryDto dto = new CourseSummaryDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setStartDate(course.getStartDate());
        dto.setEndDate(course.getEndDate());
        dto.setStatus(course.getStatus());
        dto.setMaxSeats(course.getMaxSeats());

        return dto;
    }

    public List<CourseDto> toDtoList(List<Course> courses) {
        return courses.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
} 