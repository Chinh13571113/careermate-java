package com.fpt.careermate.util;

import com.fpt.careermate.domain.JobPosting;
import com.fpt.careermate.repository.JdSkillRepo;
import com.fpt.careermate.repository.JobPostingRepo;
import com.fpt.careermate.services.dto.request.JdSkillRequest;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingValidator {

    @Autowired
    final JobPostingRepo jobPostingRepo;
    final JdSkillRepo jdSkillRepo;

    public void checkDuplicateJobPostingTitle(String title) {
        Optional<JobPosting> existingJobPosting = jobPostingRepo.findByTitle(title);
        if (existingJobPosting.isPresent()) {
            throw new AppException(ErrorCode.DUPLICATE_JOB_POSTING_TITLE);
        }
    }

    public void checkDuplicateJobPostingTitleAndNotCurrentRecruiter(String title, int id) {
        Optional<JobPosting> existingJobPosting = jobPostingRepo.findByTitle(title);
        if (existingJobPosting.isPresent()) {
            if (existingJobPosting.get().getRecruiter().getId() != id) {
                throw new AppException(ErrorCode.DUPLICATE_JOB_POSTING_TITLE);
            }
        }
    }

    public void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate.isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
        }
    }

    // Validate existing skill in JdSkill list
    public void validateJdSkill(Set<JdSkillRequest> jdSkillRequests) {
        AtomicBoolean isExisted = new AtomicBoolean(false);

        jdSkillRequests.forEach(jdskillRequest -> {
            jdSkillRepo.findAll().forEach(jdskill -> {
                if (jdskill.getId() == jdskillRequest.getId()) {
                    isExisted.set(true);
                }
            });

            if(!isExisted.get()) {
                throw new AppException(ErrorCode.JD_SKILL_NOT_FOUND);
            }

            isExisted.set(false);
        });


    }

}
