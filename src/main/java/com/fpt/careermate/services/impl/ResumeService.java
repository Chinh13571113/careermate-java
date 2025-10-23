package com.fpt.careermate.services.impl;

import com.fpt.careermate.domain.Education;
import com.fpt.careermate.services.dto.request.ResumeRequest;
import com.fpt.careermate.services.dto.response.EducationResponse;
import com.fpt.careermate.services.dto.response.ResumeResponse;

import java.util.List;

public interface ResumeService {
    ResumeResponse createResume(ResumeRequest resumeRequest);
    List<ResumeResponse> getAllResumesByCandidate();
    ResumeResponse getResumeById(int resumeId);
    void deleteResume(int resumeId);
    ResumeResponse updateResume(int resumeId, ResumeRequest resumeRequest);
}
