package com.fpt.careermate.repository;

import com.fpt.careermate.domain.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface JobDescriptionRepo extends JpaRepository<JobDescription, Integer> {
    List<JobDescription> findByJobPosting_Id(int id);
}
