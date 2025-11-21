package com.fpt.careermate.services.order_services.service;

import com.fpt.careermate.common.constant.StatusInvoice;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.order_services.domain.CandidateInvoice;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateOrderResponse;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.order_services.repository.CandidateInvoiceRepo;
import com.fpt.careermate.services.order_services.repository.CandidatePackageRepo;
import com.fpt.careermate.services.order_services.service.impl.CandidateInvoiceService;
import com.fpt.careermate.services.order_services.service.mapper.OrderMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateInvoiceImp implements CandidateInvoiceService {

    CandidateInvoiceRepo candidateInvoiceRepo;
    CandidatePackageRepo candidatePackageRepo;
    CandidateRepo candidateRepo;
    OrderMapper orderMapper;
    AuthenticationImp authenticationImp;

//    @Transactional
    public void createInvoice(String packageName, Candidate currentCandidate) {
        CandidatePackage pkg = candidatePackageRepo.findByName(packageName);

        CandidateInvoice candidateInvoice = new CandidateInvoice();
        candidateInvoice.setCandidate(currentCandidate);
        candidateInvoice.setCandidatePackage(pkg);
        candidateInvoice.setAmount(pkg.getPrice());
        candidateInvoice.setStatus(StatusInvoice.PAID);
        candidateInvoice.setActive(true);
        candidateInvoice.setStartDate(LocalDate.now());
        candidateInvoice.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));

        candidateInvoiceRepo.save(candidateInvoice);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public void cancelOrder(int id) {
        CandidateInvoice candidateInvoice = candidateInvoiceRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (candidateInvoice.getStatus().equals(StatusInvoice.PAID)) {
            candidateInvoice.setStatus(StatusInvoice.CANCELLED);
            candidateInvoice.setCancelledAt(LocalDate.now());
            candidateInvoice.setActive(false);
            candidateInvoiceRepo.save(candidateInvoice);
        }
        else {
            throw new AppException(ErrorCode.CANNOT_DELETE_ORDER);
        }
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public MyCandidateOrderResponse myCandidatePackage() {
        Candidate currentCandidate = getCurrentCandidate();

        Optional<CandidateInvoice> candidateOrder = candidateInvoiceRepo.findByCandidate_CandidateIdAndIsActiveTrue(currentCandidate.getCandidateId());

        if(candidateOrder.isEmpty()) throw new AppException(ErrorCode.USING_FREE_PACAKGE);

        return orderMapper.toOrderResponse(candidateOrder.get());
    }
    
    private Candidate getCurrentCandidate(){
        Account currentAccount = authenticationImp.findByEmail();
        Optional<Candidate> candidate = candidateRepo.findByAccount_Id(Integer.valueOf(currentAccount.getId()));
        Candidate currentCandidate = candidate.get();
        return currentCandidate;
    }

    public void updateCandidateOrder(CandidateInvoice exstingCandidateInvoice, String packageName){
        CandidatePackage pkg = candidatePackageRepo.findByName(packageName);

        exstingCandidateInvoice.setCandidatePackage(pkg);
        exstingCandidateInvoice.setAmount(pkg.getPrice());
        exstingCandidateInvoice.setStatus(StatusInvoice.PAID);
        exstingCandidateInvoice.setActive(true);
        exstingCandidateInvoice.setStartDate(LocalDate.now());
        exstingCandidateInvoice.setEndDate(LocalDate.now().plusDays(pkg.getDurationDays()));
        exstingCandidateInvoice.setCancelledAt(null);

        candidateInvoiceRepo.save(exstingCandidateInvoice);
    }

    public CandidatePackage getPackageByName(String packageName){
        return candidatePackageRepo.findByName(packageName);
    }

    // Check if candidate has an active order
    public boolean hasActivePackage() {
        Candidate currentCandidate = getCurrentCandidate();
        Optional<CandidateInvoice> activeOrder = candidateInvoiceRepo.findByCandidate_CandidateIdAndIsActiveTrue(currentCandidate.getCandidateId());
        return activeOrder.isPresent();
    }
}
