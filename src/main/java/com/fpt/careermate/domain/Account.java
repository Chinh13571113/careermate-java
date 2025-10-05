package com.fpt.careermate.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    String username;
    @Column(name = "email", unique = true)
    String email;
    String password;
    @Column(name = "status")
    String status;

    @ManyToMany
    Set<Role> roles;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    ForgotPassword forgotPassword;

}
