package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CandidateProfileService {
    void createProfile(CandidateProfileRequest request);
    PageResponse<CandidateProfileResponse> findAll(Pageable pageable);
    CandidateProfileResponse updateProfile(CandidateProfileRequest request);
    void deleteProfile(int id);
}
