package com.fpt.careermate.services.order_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "candidate_entitlement_package")
public class CandidateEntitlementPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    boolean enabled;
    int limitValue;
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "entitlement_id")
    CandidateEntitlement candidateEntitlement;

    @ManyToOne
    @JoinColumn(name = "package_id")
    CandidatePackage candidatePackage;

    public CandidateEntitlementPackage(
            boolean enabled,
            int limitValue,
            LocalDateTime createdAt,
            CandidateEntitlement candidateEntitlement,
            CandidatePackage candidatePackage
    ) {
        this.enabled = enabled;
        this.limitValue = limitValue;
        this.createdAt = createdAt;
        this.candidateEntitlement = candidateEntitlement;
        this.candidatePackage = candidatePackage;
    }
}
