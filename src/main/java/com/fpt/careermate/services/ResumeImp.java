package com.fpt.careermate.services;

import com.fpt.careermate.domain.*;
import com.fpt.careermate.repository.CandidateRepo;
import com.fpt.careermate.repository.ResumeRepo;
import com.fpt.careermate.services.dto.request.*;
import com.fpt.careermate.services.dto.response.ResumeResponse;
import com.fpt.careermate.services.impl.ResumeService;
import com.fpt.careermate.services.mapper.ResumeMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ResumeImp implements ResumeService {

    ResumeRepo resumeRepo;
    CandidateRepo candidateRepo;
    ResumeMapper resumeMapper;
    CandidateProfileImp candidateProfileImp;
    AuthenticationImp authenticationService;

    @Override
    @Transactional
    public ResumeResponse createResume(ResumeRequest resumeRequest) {
        Candidate candidate = candidateProfileImp.generateProfile();

        // Create new resume
        Resume newResume = new Resume();
        newResume.setCandidate(candidate);
        newResume.setAboutMe(resumeRequest.getAboutMe());

        Resume savedResume = resumeRepo.save(newResume);
        return resumeMapper.toResumeResponse(savedResume);
    }

    @Override
    public List<ResumeResponse> getAllResumesByCandidate() {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find all resumes for this candidate
        List<Resume> resumes = resumeRepo.findByCandidateCandidateId(candidate.getCandidateId());

        return resumes.stream()
                .map(resumeMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ResumeResponse getResumeById(int resumeId) {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find resume by ID and ensure it belongs to the authenticated candidate
        Resume resume = resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        return resumeMapper.toResumeResponse(resume);
    }

    @Transactional
    @Override
    public void deleteResume(int resumeId) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        resumeRepo.deleteById(resumeId);
    }

    @Transactional
    @Override
    public ResumeResponse updateResume(int resumeId, ResumeRequest resumeRequest) {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find resume and ensure it belongs to the authenticated candidate
        Resume resume = resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        // Update resume
        resume.setAboutMe(resumeRequest.getAboutMe());
        Resume updatedResume = resumeRepo.save(resume);

        return resumeMapper.toResumeResponse(updatedResume);
    }

    // Helper method to get resume by ID for other services (used by Education, Certificate, etc.)
    public Resume getResumeEntityById(int resumeId) {
        Candidate candidate = candidateProfileImp.generateProfile();
        return resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
    }
}
