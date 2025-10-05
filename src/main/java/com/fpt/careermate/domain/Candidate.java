package com.fpt.careermate.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "candidate")
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int candidateId;
    LocalDateTime dob;
    String title;
    String phone;
    String address;
    String image;
    String gender;
    String link;




    // One-to-one với Account
    @OneToOne
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

}
