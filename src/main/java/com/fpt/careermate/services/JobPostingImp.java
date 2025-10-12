package com.fpt.careermate.services;

import com.fpt.careermate.constant.StatusJobPosting;
import com.fpt.careermate.domain.*;
import com.fpt.careermate.repository.JdSkillRepo;
import com.fpt.careermate.repository.JobDescriptionRepo;
import com.fpt.careermate.repository.JobPostingRepo;
import com.fpt.careermate.repository.RecruiterRepo;
import com.fpt.careermate.services.dto.request.JdSkillRequest;
import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.dto.response.JobPostingSkillResponse;
import com.fpt.careermate.services.impl.JobPostingService;
import com.fpt.careermate.services.mapper.JobPostingMapper;
import com.fpt.careermate.util.JobPostingValidator;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobPostingImp implements JobPostingService {

    JobPostingRepo jobPostingRepo;
    RecruiterRepo recruiterRepo;
    JdSkillRepo jdSkillRepo;
    JobDescriptionRepo jobDescriptionRepo;
    JobPostingMapper jobPostingMapper;
    AuthenticationImp authenticationImp;
    JobPostingValidator jobPostingValidator;

    // Recruiter create job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void createJobPosting(JobPostingCreationRequest request) {
        // Validate request
        jobPostingValidator.checkDuplicateJobPostingTitle(request.getTitle());
        jobPostingValidator.validateExpirationDate(request.getExpirationDate());

        Recruiter recruiter = getMyRecruiter();

        JobPosting jobPosting = jobPostingMapper.toJobPosting(request);
        jobPosting.setCreateAt(LocalDate.now());
        jobPosting.setRecruiter(recruiter);
        jobPosting.setStatus(StatusJobPosting.PENDING);

        jobPostingRepo.save(jobPosting);

        Set<JobDescription> jobDescriptions = new HashSet<>();
        for(JdSkillRequest skillReq : request.getJdSkills()) {
//            a) find jdSkill by id
            Optional<JdSkill> exstingJdSkill = jdSkillRepo.findById(skillReq.getId());
            JdSkill jdSkill = exstingJdSkill.get();

//            b) Create JobDescription link
            JobDescription jd = new JobDescription();
            jd.setJobPosting(jobPosting);
            jd.setJdSkill(jdSkill);
            jd.setMustToHave(skillReq.isMustToHave());

            jobDescriptions.add(jd);
        }

//        Save all JobDescription
        jobDescriptionRepo.saveAll(jobDescriptions);

        jobPosting.setJobDescriptions(jobDescriptions);

        jobPostingRepo.save(jobPosting);
    }

    // Get all job postings of the current recruiter with all status
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public List<JobPostingForRecruiterResponse> getAllJobPostingForRecruiter() {
        Recruiter recruiter = getMyRecruiter();

        return jobPostingMapper.
                toJobPostingForRecruiterResponseList(jobPostingRepo.findAllByRecruiter_Id(recruiter.getId()));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public JobPostingForRecruiterResponse getJobPostingDetailForRecruiter(int id){
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);
        JobPostingForRecruiterResponse jpResponse= jobPostingMapper.toJobPostingDetailForRecruiterResponse(jobPosting);

        Set<JobPostingSkillResponse> jobPostingSkillResponses = jobPostingMapper.toJobPostingSkillResponseSet(jobPosting.getJobDescriptions());
        jobPostingSkillResponses.forEach(jobPostingSkillResponse -> {
            jobPosting.getJobDescriptions().forEach(jd -> {
                jobPostingSkillResponse.setName(jd.getJdSkill().getName());
            });
        });

        jpResponse.setSkills(jobPostingSkillResponses);

        return jpResponse;
    }

    // Recruiter update job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void updateJobPosting(int id, JobPostingCreationRequest request) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Check job posting status
        if(Set.of(
                StatusJobPosting.DELETED,
                StatusJobPosting.ACTIVE,
                StatusJobPosting.PAUSED,
                StatusJobPosting.EXPIRED
        ).contains(jobPosting.getStatus())) throw new AppException(ErrorCode.CANNOT_MODIFY_JOB_POSTING);

        // Validate request
        jobPostingValidator.checkDuplicateJobPostingTitleAndNotCurrentRecruiter(request.getTitle(), jobPosting.getRecruiter().getId());
        jobPostingValidator.validateExpirationDate(request.getExpirationDate());
        // Validate JdSkill exist
        jobPostingValidator.validateJdSkill(request.getJdSkills());

        // Remove all old job descriptions
        List<JobDescription> jobDescriptions = jobDescriptionRepo.findByJobPosting_Id(jobPosting.getId());
        jobDescriptionRepo.deleteAll(jobDescriptions);

        // Update job posting
        jobPosting.setTitle(request.getTitle());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setAddress(request.getAddress());
        jobPosting.setExpirationDate(request.getExpirationDate());

        // Add new job descriptions
        Set<JobDescription> newJobDescriptions = new HashSet<>();
        request.getJdSkills().forEach(jd -> {
            Optional<JdSkill> existingJdSkill = jdSkillRepo.findById(jd.getId());
            JdSkill jdSkill = existingJdSkill.get();

            JobDescription jobDescription = new JobDescription();
            jobDescription.setJobPosting(jobPosting);
            jobDescription.setJdSkill(jdSkill);
            jobDescription.setMustToHave(jd.isMustToHave());

            newJobDescriptions.add(jobDescription);
        });
        jobPosting.setJobDescriptions(newJobDescriptions);

        jobPostingRepo.save(jobPosting);
    }

    // Recruiter delete job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void deleteJobPosting(int id) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Check job posting status
        if(Set.of(
                StatusJobPosting.DELETED,
                StatusJobPosting.ACTIVE,
                StatusJobPosting.PAUSED
        ).contains(jobPosting.getStatus())) throw new AppException(ErrorCode.CANNOT_DELETE_JOB_POSTING);

        jobPosting.setStatus(StatusJobPosting.DELETED);
        jobPostingRepo.save(jobPosting);
    }

    // Recruiter pause job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void pauseJobPosting(int id) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Check job posting status
        if(!jobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) throw new AppException(ErrorCode.CANNOT_PAUSE_JOB_POSTING);

        jobPosting.setStatus(StatusJobPosting.PAUSED);
        jobPostingRepo.save(jobPosting);
    }

    private JobPosting findJobPostingEntityForRecruiterById(int id){
        Recruiter recruiter = getMyRecruiter();

        // Check job posting exist
        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Check job posting belong to current recruiter
        if(jobPosting.getRecruiter().getId() != recruiter.getId()){
            throw new AppException(ErrorCode.JOB_POSTING_FORBIDDEN);
        }

        return jobPosting;
    }

    // Get current recruiter
    private Recruiter getMyRecruiter(){
        Account currentAccount = authenticationImp.findByEmail();
        Optional<Recruiter> currentRecruiter = recruiterRepo.findByAccount_Id(currentAccount.getId());
        return currentRecruiter.get();
    }

}
