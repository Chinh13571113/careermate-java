package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepo extends JpaRepository<Candidate, Integer> {
}
