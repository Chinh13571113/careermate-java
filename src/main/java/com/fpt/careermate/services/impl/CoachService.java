package com.fpt.careermate.services.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.services.dto.response.CourseResponse;

public interface CoachService {
    CourseResponse generateCourse(String topic);
    String generateLesson(int lessonId) throws JsonProcessingException;
}
