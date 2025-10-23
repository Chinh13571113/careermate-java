package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepo extends JpaRepository<Resume, Integer> {
    List<Resume> findByCandidateCandidateId(int candidateId);
    Optional<Resume> findByResumeIdAndCandidateCandidateId(int resumeId, int candidateId);
}