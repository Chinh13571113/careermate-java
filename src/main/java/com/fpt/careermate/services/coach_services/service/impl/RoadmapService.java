package com.fpt.careermate.services.coach_services.service.impl;


import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicDetailResponse;

public interface RoadmapService {
    void addRoadmap(String nameRoadmap, String fileName);
    RoadmapResponse getRoadmap(int roadmapId);
    TopicDetailResponse getTopicDetail(int topicId);
}
