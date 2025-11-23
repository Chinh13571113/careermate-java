package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.CandidateInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CandidateInvoiceRepo extends JpaRepository<CandidateInvoice,Integer> {
    Optional<CandidateInvoice> findByCandidate_CandidateIdAndIsActiveTrue(int candidateId);

    @Override
    Page<CandidateInvoice> findAll(Pageable pageable);
}
