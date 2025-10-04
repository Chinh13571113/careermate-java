package com.fpt.careermate.services;

import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.Candidate;
import com.fpt.careermate.repository.CandidateRepo;
import com.fpt.careermate.services.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.dto.response.PageResponse;
import com.fpt.careermate.services.impl.CandidateProfileService;
import com.fpt.careermate.services.mapper.CandidateMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateProfileImp implements CandidateProfileService {
    CandidateRepo candidateRepo;
    CandidateMapper candidateMapper;
    AuthenticationImp authenticationService;


    @Override
    public void createProfile(CandidateProfileRequest request) {
        //get account id from security context
        Candidate candidate = candidateMapper.toCandidate(request);
        Account account = authenticationService.findByEmail();
        candidate.setAccount(account);
        candidateRepo.save(candidate);
    }

    @Override
    public PageResponse<CandidateProfileResponse> findAll(Pageable pageable) {
        //role admin can get any profile by id
        Page<Candidate> candidatePage = candidateRepo.findAll(pageable);
        return new PageResponse<>(
                candidatePage.getContent()
                        .stream()
                        .map(candidateMapper::toCandidateProfileResponse)
                        .toList(),
                candidatePage.getNumber(),
                candidatePage.getSize(),
                candidatePage.getTotalElements(),
                candidatePage.getTotalPages()
        );    }

    @Override
    public CandidateProfileResponse updateProfile(CandidateProfileRequest request) {
        return null;
    }

    @Override
    public void deleteProfile(int id) {
        candidateRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));
        candidateRepo.deleteById(id);
    }

}
