package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.ResumeImp;
import com.fpt.careermate.services.dto.request.ResumeRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.ResumeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@Tag(name = "Resume", description = "Resume API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ResumeController {
    ResumeImp resumeImp;

    @PostMapping
    @Operation(summary = "Create Resume", description = "Create a new resume")
    ApiResponse<ResumeResponse> createResume(@RequestBody ResumeRequest resumeRequest) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.createResume(resumeRequest))
                .message("Create resume successfully")
                .build();
    }

    @GetMapping
    @Operation(summary = "Get All Resumes by Candidate", description = "Retrieve all resumes for the authenticated candidate")
    ApiResponse<List<ResumeResponse>> getAllResumesByCandidate() {
        return ApiResponse.<List<ResumeResponse>>builder()
                .result(resumeImp.getAllResumesByCandidate())
                .message("Get resumes successfully")
                .build();
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Get Resume by ID", description = "Retrieve a specific resume by ID")
    ApiResponse<ResumeResponse> getResumeById(@PathVariable int resumeId) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.getResumeById(resumeId))
                .message("Get resume successfully")
                .build();
    }

    @DeleteMapping("/{resumeId}")
    @Operation(summary = "Delete Resume", description = "Delete a resume by ID")
    ApiResponse<Void> deleteResume(@PathVariable int resumeId) {
        resumeImp.deleteResume(resumeId);
        return ApiResponse.<Void>builder()
                .message("Delete resume successfully")
                .build();
    }

    @PutMapping("/{resumeId}")
    @Operation(summary = "Update Resume", description = "Update an existing resume")
    ApiResponse<ResumeResponse> updateResume(@PathVariable int resumeId, @RequestBody ResumeRequest resumeRequest) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.updateResume(resumeId, resumeRequest))
                .message("Update resume successfully")
                .build();
    }
}
