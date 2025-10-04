package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Account;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepo extends JpaRepository<Account,Integer> {
    boolean existsByEmail(String email);

    Optional<Account> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("update account a set a.password = ?2 where a.email = ?1")
    void updatePassword(String email, String newPassword);
}
