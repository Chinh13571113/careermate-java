package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.CandidateEntitlementPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CandidateEntitlementPackageRepo extends JpaRepository<CandidateEntitlementPackage,Integer> {
    CandidateEntitlementPackage findByCandidatePackage_NameAndCandidateEntitlement_Code(String packageName, String entitlementCode);
}
