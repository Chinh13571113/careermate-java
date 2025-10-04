package com.fpt.careermate.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "forgot_password")
public class ForgotPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int fpid;

    @Column(nullable = false)
    int otp;
    @Column(nullable = false)
    Date expiredAt;

    @OneToOne
    Account account;
}
