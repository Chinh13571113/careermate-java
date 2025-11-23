package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.CandidateEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface CandidateEntitlementRepo extends JpaRepository<CandidateEntitlement,Integer> {
    CandidateEntitlement findByCode(String code);
}
