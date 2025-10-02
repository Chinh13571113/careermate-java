package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Order;
import com.fpt.careermate.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment,Integer> {
    Optional<Payment> findByOrder(Order order);
}
