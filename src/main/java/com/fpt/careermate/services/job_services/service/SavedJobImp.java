package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.domain.SavedJob;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.job_services.repository.SavedJobRepo;
import com.fpt.careermate.services.job_services.service.impl.SavedJobService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SavedJobImp implements SavedJobService {

    SavedJobRepo savedJobRepo;
    JobPostingRepo jobPostingRepo;
    CoachUtil coachUtil;

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public boolean toggleSaveJob(int jobId){
        int candidateId = coachUtil.getCurrentCandidate().getCandidateId();
        Optional<SavedJob> savedJobOpt =
                savedJobRepo.findByCandidate_candidateIdAndJobPosting_Id(candidateId, jobId);
        // Check if job posting exists
        Optional<JobPosting> jobPosting = jobPostingRepo.findById(jobId);
        if (jobPosting.isEmpty()) {
            throw new AppException(ErrorCode.JOB_POSTING_NOT_FOUND);
        }

        if (savedJobOpt.isPresent()) {
            savedJobRepo.delete(savedJobOpt.get());
            return false; // Job unsaved
        } else {
            SavedJob savedJob = new SavedJob();
            savedJob.setCandidate(coachUtil.getCurrentCandidate());
            savedJob.setJobPosting(jobPosting.get());
            savedJob.setSavedAt(LocalDateTime.now());
            savedJobRepo.save(savedJob);
            return true; // Job saved
        }
    }

}
