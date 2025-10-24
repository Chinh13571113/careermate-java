package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.JobApplyRequest;
import com.fpt.careermate.services.dto.response.JobApplyResponse;

import java.util.List;

public interface JobApplyService {
    JobApplyResponse createJobApply(JobApplyRequest request);
    JobApplyResponse getJobApplyById(int id);
    List<JobApplyResponse> getAllJobApplies();
    List<JobApplyResponse> getJobAppliesByJobPosting(int jobPostingId);
    List<JobApplyResponse> getJobAppliesByCandidate(int candidateId);
    JobApplyResponse updateJobApply(int id, String request);
    void deleteJobApply(int id);
}
