package com.fpt.careermate.repository;

import com.fpt.careermate.domain.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;



public interface JobDescriptionRepo extends JpaRepository<JobDescription, Integer> {

}
