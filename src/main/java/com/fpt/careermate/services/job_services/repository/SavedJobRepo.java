package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedJobRepo extends JpaRepository<SavedJob, Integer> {
    Optional<SavedJob> findByCandidate_candidateIdAndJobPosting_Id(int candidateId, int jobId);
}
