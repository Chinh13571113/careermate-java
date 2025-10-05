package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Candidate;
import com.fpt.careermate.services.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.dto.response.CandidateProfileResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface CandidateMapper {
    @Mapping(target = "account", ignore = true)
    Candidate toCandidate(CandidateProfileRequest candidate);
    CandidateProfileResponse toCandidateProfileResponse(Candidate candidate);
}
