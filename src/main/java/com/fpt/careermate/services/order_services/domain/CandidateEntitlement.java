package com.fpt.careermate.services.order_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "candidate_entitlement")
public class CandidateEntitlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;
    String code;
    String unit;
    boolean hasLimit;

    @OneToMany(mappedBy = "candidateEntitlement")
    List<CandidateEntitlementPackage> candidateEntitlementPackages;
}
