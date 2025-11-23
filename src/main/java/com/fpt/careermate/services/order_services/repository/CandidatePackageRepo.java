package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidatePackageRepo extends JpaRepository<CandidatePackage,Integer> {
    CandidatePackage findByName(String name);
}
