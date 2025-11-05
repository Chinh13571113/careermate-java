package com.fpt.careermate.services.coach_services.service.mapper;

import com.fpt.careermate.services.coach_services.domain.Course;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    Course toCourse(CourseCreationRequest request);
}