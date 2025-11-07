package com.fpt.careermate.services.coach_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.coach_services.service.CourseImp;
import com.fpt.careermate.services.coach_services.service.RoadmapImp;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.CoursePageResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RecommendedCourseResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/roadmap")
@Tag(name = "Roadmap", description = "Manage roadmap")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoadmapController {

    RoadmapImp roadmapImp;

    @PostMapping()
    @Operation(description = """
            Do not use this API
            Need login as ADMIN to access
            """)
    public ApiResponse<Void> addRoadmap(
            @RequestParam String nameRoadmap,
            @RequestParam String fileName)
    {
        roadmapImp.addRoadmap(nameRoadmap, fileName);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/{roadmapId}")
    @Operation(description = """
            Get roadmap by id
            input: roadmapId
            output: RoadmapResponse have topics and subtopics
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<RoadmapResponse> getRoadmap(@PathVariable int roadmapId)
    {
        return ApiResponse.<RoadmapResponse>builder()
                .result(roadmapImp.getRoadmap(roadmapId))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/topic/{topicId}")
    @Operation(description = """
            Get topic by id
            input: topicId
            output: name, description and resources of topic
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<TopicDetailResponse> getTopicDetailById(@PathVariable int topicId)
    {
        return ApiResponse.<TopicDetailResponse>builder()
                .result(roadmapImp.getTopicDetail(topicId))
                .code(200)
                .message("success")
                .build();
    }
}