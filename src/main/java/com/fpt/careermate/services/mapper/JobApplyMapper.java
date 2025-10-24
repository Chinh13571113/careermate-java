package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.JobApply;
import com.fpt.careermate.services.dto.response.JobApplyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobApplyMapper {

    @Mapping(target = "jobPostingId", source = "jobPosting.id")
    @Mapping(target = "jobTitle", source = "jobPosting.title")
    @Mapping(target = "jobDescription", source = "jobPosting.description")
    @Mapping(target = "expirationDate", source = "jobPosting.expirationDate")
    @Mapping(target = "candidateId", source = "candidate.candidateId")
    JobApplyResponse toJobApplyResponse(JobApply jobApply);

    default String getCandidateName(JobApply jobApply) {
        if (jobApply.getCandidate() != null) {
            return jobApply.getCandidate().getFullName();
        }
        return null;
    }
}
