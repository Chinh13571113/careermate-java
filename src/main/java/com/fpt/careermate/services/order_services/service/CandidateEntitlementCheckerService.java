package com.fpt.careermate.services.order_services.service;


import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.order_services.domain.EntitlementPackage;
import com.fpt.careermate.services.order_services.domain.Order;
import com.fpt.careermate.services.order_services.domain.Package;
import com.fpt.careermate.services.order_services.repository.EntitlementPackageRepo;
import com.fpt.careermate.services.order_services.repository.PackageRepo;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service kiểm tra quyền hạn của Candidate khi mua gói dịch vụ
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateEntitlementCheckerService {

    EntitlementPackageRepo entitlementPackageRepo;
    CoachUtil coachUtil;
    PackageRepo packageRepo;

    String AI_ROADMAP = "AI_ROADMAP";

    /**
     * Kiểm tra candidate có quyền dùng tính năng Roadmap Recommendation không?
     */
    public boolean canUseRoadmapRecommendation() {
        // Kiểm tra gói Free
        if (checkFreePackage()) {
            // Nếu là Free package
            log.info("Candidate is on Free Package");
            Package freePackage = packageRepo.findByName("Free");
            EntitlementPackage entitlement = entitlementPackageRepo
                    .findByCandidatePackage_NameAndEntitlement_Code(freePackage.getName(), AI_ROADMAP);
            return entitlement != null && entitlement.isEnabled();
        }

        Package currentPackage = coachUtil.getCurrentCandidate().getOrder().getCandidatePackage();
        log.info("Current Package Name: " + currentPackage.getName());

        // Lấy entitlement "AI_ROADMAP"
        EntitlementPackage entitlement = entitlementPackageRepo
                .findByCandidatePackage_NameAndEntitlement_Code(currentPackage.getName(), AI_ROADMAP);

        // Trả kết quả
        return entitlement != null && entitlement.isEnabled();
    }

    // Khi có candidate mới, kiểm tra order == null hoặc active == false là Free
    private boolean checkFreePackage() {
        Candidate currentCandidate = coachUtil.getCurrentCandidate();
        Order order = currentCandidate.getOrder();

        if(order == null || !order.isActive()) {
            return true;
        }

        return false;
    }
}
