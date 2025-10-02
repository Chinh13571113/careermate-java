package com.fpt.careermate.domain;

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
@Entity(name = "test_candidates")
// TODO: remove this entity later
public class TestCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    //    default: EAGER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    Package currentPackage;

    @OneToMany(mappedBy = "candidate")
    List<Order> orders;
}
