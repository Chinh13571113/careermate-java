package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface OrderRepo extends JpaRepository<Order,Integer> {
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByCandidate_CandidateId(int candidateId);
}
