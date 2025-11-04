package com.fpt.careermate.services.recommendation.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.recommendation.dto.CandidateRecommendationDTO;
import com.fpt.careermate.services.recommendation.dto.RecommendationResponseDTO;
import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.domain.Skill;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateRecommendationServiceImpl implements CandidateRecommendationService {

    WeaviateClient weaviateClient;
    JobPostingRepo jobPostingRepo;
    CandidateRepo candidateRepo;
    ResumeRepo resumeRepo;

    private static final String CANDIDATE_CLASS = "CandidateProfile";
    private static final int DEFAULT_MAX_CANDIDATES = 10;
    private static final double DEFAULT_MIN_MATCH_SCORE = 0.3; // Lower threshold for better results

    @Override
    @Transactional(readOnly = true)
    public RecommendationResponseDTO getRecommendedCandidatesForJob(
            int jobPostingId,
            Integer maxCandidates,
            Double minMatchScore
    ) {
        long startTime = System.currentTimeMillis();

        // Validate and get job posting
        JobPosting jobPosting = jobPostingRepo.findById(jobPostingId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Extract required skills from job descriptions
        List<String> requiredSkills = new ArrayList<>();

        if (jobPosting.getJobDescriptions() != null && !jobPosting.getJobDescriptions().isEmpty()) {
            requiredSkills = jobPosting.getJobDescriptions().stream()
                    .filter(jd -> jd.getJdSkill() != null)
                    .map(jd -> jd.getJdSkill().getName())
                    .filter(name -> name != null && !name.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
        }

        // If no skills from job descriptions, try to extract from job description text
        if (requiredSkills.isEmpty() && jobPosting.getDescription() != null) {
            log.info("⚠️ No job descriptions found, using description text for job posting ID: {}", jobPostingId);
            // Use the entire job description as search query
            requiredSkills = Arrays.asList(jobPosting.getDescription().split("\\s+")).stream()
                    .filter(word -> word.length() > 3) // Filter meaningful words
                    .limit(20) // Limit to top 20 keywords
                    .collect(Collectors.toList());
            log.info("📝 Extracted {} keywords from description: {}", requiredSkills.size(),
                    String.join(", ", requiredSkills.subList(0, Math.min(5, requiredSkills.size()))));
        }

        if (requiredSkills.isEmpty()) {
            log.warn("❌ No skills or description text found for job posting ID: {}", jobPostingId);
            return RecommendationResponseDTO.builder()
                    .jobPostingId(jobPostingId)
                    .jobTitle(jobPosting.getTitle())
                    .totalCandidatesFound(0)
                    .recommendations(Collections.emptyList())
                    .processingTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        log.info("🎯 Searching for candidates with {} required skills: {}",
                requiredSkills.size(), String.join(", ", requiredSkills));

        // Set defaults
        int limit = maxCandidates != null ? maxCandidates : DEFAULT_MAX_CANDIDATES;
        double threshold = minMatchScore != null ? minMatchScore : DEFAULT_MIN_MATCH_SCORE;

        // Search in Weaviate using vector similarity
        List<CandidateRecommendationDTO> recommendations = searchCandidatesInWeaviate(
                requiredSkills,
                jobPosting.getYearsOfExperience(),
                limit,
                threshold
        );

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Found {} recommended candidates for job '{}' in {}ms",
                recommendations.size(), jobPosting.getTitle(), processingTime);

        return RecommendationResponseDTO.builder()
                .jobPostingId(jobPostingId)
                .jobTitle(jobPosting.getTitle())
                .totalCandidatesFound(recommendations.size())
                .recommendations(recommendations)
                .processingTimeMs(processingTime)
                .build();
    }

    private List<CandidateRecommendationDTO> searchCandidatesInWeaviate(
            List<String> requiredSkills,
            int minYearsExperience,
            int limit,
            double threshold
    ) {
        try {
            log.info("🔎 Searching Weaviate for candidates with skills: {} (minExp: {}, limit: {})",
                    String.join(", ", requiredSkills), minYearsExperience, limit);

            Field[] fields = new Field[]{
                    Field.builder().name("candidateId").build(),
                    Field.builder().name("candidateName").build(),
                    Field.builder().name("email").build(),
                    Field.builder().name("skills").build(),
                    Field.builder().name("totalExperience").build(),
                    Field.builder().name("aboutMe").build()
            };

            // Perform query - fetch all candidates (we'll filter by skills manually)
            // Note: We fetch more than limit to have enough after skill filtering
            Result<GraphQLResponse> result = weaviateClient.graphQL().get()
                    .withClassName(CANDIDATE_CLASS)
                    .withLimit(Math.max(limit * 5, 100)) // Fetch more to filter by skills
                    .withFields(fields)
                    .run();

            if (result.hasErrors()) {
                log.error("❌ Weaviate search error: {}", result.getError().getMessages());
                return Collections.emptyList();
            }

            log.info("✅ Weaviate search completed, filtering and ranking results...");
            // Parse and filter results based on skill matching
            List<CandidateRecommendationDTO> recommendations = parseAndRankCandidates(
                    result.getResult(), requiredSkills, minYearsExperience, limit, threshold);
            log.info("📈 Found {} matching candidates", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("❌ Error searching candidates in Weaviate: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<CandidateRecommendationDTO> parseAndRankCandidates(
            GraphQLResponse response,
            List<String> requiredSkills,
            int minYearsExperience,
            int limit,
            double threshold
    ) {
        List<CandidateRecommendationDTO> recommendations = new ArrayList<>();

        try {
            Object dataObj = response.getData();
            if (dataObj == null || !(dataObj instanceof Map)) return recommendations;

            Map<String, Object> data = (Map<String, Object>) dataObj;
            Object getObj = data.get("Get");
            if (getObj == null || !(getObj instanceof Map)) return recommendations;

            Map<String, Object> get = (Map<String, Object>) getObj;
            Object candidatesObj = get.get(CANDIDATE_CLASS);
            if (candidatesObj == null || !(candidatesObj instanceof List)) return recommendations;

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) candidatesObj;

            log.info("📦 Processing {} candidates from Weaviate", candidates.size());

            for (Map<String, Object> candidate : candidates) {
                try {
                    Object candidateIdObj = candidate.get("candidateId");
                    if (candidateIdObj == null) continue;
                    int candidateId = ((Number) candidateIdObj).intValue();

                    String candidateName = (String) candidate.get("candidateName");
                    String email = (String) candidate.get("email");

                    Object skillsObj = candidate.get("skills");
                    List<String> candidateSkills = (skillsObj instanceof List)
                            ? (List<String>) skillsObj
                            : Collections.emptyList();

                    Object expObj = candidate.get("totalExperience");
                    int totalExperience = expObj != null ? ((Number) expObj).intValue() : 0;

                    String aboutMe = (String) candidate.get("aboutMe");

                    // Note: We don't filter by experience as a hard requirement.
                    // Instead, experience is used as a secondary ranking factor after skill matching.
                    // This allows candidates with matching skills but less experience to still be recommended.
                    // Recruiters can see the experience level and make their own decision.

                    // Calculate matched and missing skills (case-insensitive matching)
                    Set<String> candidateSkillSet = candidateSkills.stream()
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet());

                    List<String> matchedSkills = requiredSkills.stream()
                            .filter(skill -> candidateSkillSet.contains(skill.toLowerCase()))
                            .collect(Collectors.toList());

                    List<String> missingSkills = requiredSkills.stream()
                            .filter(skill -> !candidateSkillSet.contains(skill.toLowerCase()))
                            .collect(Collectors.toList());

                    // Calculate match score based on skill overlap
                    double matchScore = requiredSkills.isEmpty() ? 1.0 :
                            (double) matchedSkills.size() / requiredSkills.size();

                    // Apply minMatchScore threshold
                    if (matchScore < threshold) {
                        log.debug("⏭️ Skipping candidate {} - match score {} below threshold {}",
                                candidateId, String.format("%.2f", matchScore), String.format("%.2f", threshold));
                        continue;
                    }

                    log.debug("✅ Candidate {} matched {}/{} skills (score: {})",
                            candidateId, matchedSkills.size(), requiredSkills.size(),
                            String.format("%.2f", matchScore));

                    // Build recommendation DTO
                    CandidateRecommendationDTO recommendation = CandidateRecommendationDTO.builder()
                            .candidateId(candidateId)
                            .candidateName(candidateName)
                            .email(email)
                            .matchScore(matchScore)
                            .matchedSkills(matchedSkills)
                            .missingSkills(missingSkills)
                            .totalYearsExperience(totalExperience)
                            .profileSummary(aboutMe)
                            .build();

                    recommendations.add(recommendation);

                } catch (Exception e) {
                    log.warn("⚠️ Error parsing candidate data: {}", e.getMessage());
                }
            }

            // Sort by match score (descending) and then by years of experience (descending)
            recommendations.sort((a, b) -> {
                int scoreCompare = Double.compare(b.getMatchScore(), a.getMatchScore());
                if (scoreCompare != 0) return scoreCompare;
                return Integer.compare(b.getTotalYearsExperience(), a.getTotalYearsExperience());
            });

            // Return top N results
            return recommendations.stream()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error parsing Weaviate results: {}", e.getMessage(), e);
        }

        return recommendations;
    }

    @Override
    @Transactional
    public void syncCandidateToWeaviate(int candidateId) {
        try {
            Candidate candidate = candidateRepo.findById(candidateId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // Get candidate's resume
            Resume resume = resumeRepo.findByCandidate_CandidateId(candidateId)
                    .orElse(null);

            if (resume == null) {
                log.warn("No resume found for candidate ID: {}", candidateId);
                return;
            }

            // Extract skills
            List<String> skills = resume.getSkills().stream()
                    .map(Skill::getSkillName)
                    .collect(Collectors.toList());

            log.info("📋 Syncing candidate {} with {} skills: {}",
                    candidateId, skills.size(), String.join(", ", skills));

            // Calculate total years of experience from work history
            int totalExperience = resume.getWorkExperiences().stream()
                    .mapToInt(we -> {
                        if (we.getStartDate() != null && we.getEndDate() != null) {
                            return (int) java.time.temporal.ChronoUnit.YEARS.between(
                                    we.getStartDate(), we.getEndDate()
                            );
                        }
                        return 0;
                    })
                    .sum();

            // Build candidate profile object for Weaviate
            Map<String, Object> properties = new HashMap<>();
            properties.put("candidateId", candidateId);
            properties.put("candidateName", candidate.getFullName());
            properties.put("email", candidate.getAccount().getEmail());
            properties.put("skills", skills);
            properties.put("totalExperience", totalExperience);
            properties.put("aboutMe", resume.getAboutMe() != null ? resume.getAboutMe() : "");
            properties.put("syncedAt", new Date().toString());

            String weaviateId = UUID.nameUUIDFromBytes(String.valueOf(candidateId).getBytes()).toString();

            // Delete existing entry if it exists
            try {
                Result<Boolean> deleteResult = weaviateClient.data().deleter()
                        .withClassName(CANDIDATE_CLASS)
                        .withID(weaviateId)
                        .run();

                if (deleteResult.hasErrors()) {
                    log.debug("No existing entry to delete for candidate {}", candidateId);
                } else {
                    log.info("🗑️ Deleted existing Weaviate entry for candidate {}", candidateId);
                }
            } catch (Exception e) {
                log.debug("No existing entry for candidate {}: {}", candidateId, e.getMessage());
            }

            // Create fresh entry in Weaviate
            Result<WeaviateObject> result = weaviateClient.data().creator()
                    .withClassName(CANDIDATE_CLASS)
                    .withID(weaviateId)
                    .withProperties(properties)
                    .run();

            if (result.hasErrors()) {
                log.error("Failed to sync candidate {} to Weaviate: {}",
                        candidateId, result.getError().getMessages());
            } else {
                log.info("✅ Synced candidate {} to Weaviate successfully with {} skills",
                        candidateId, skills.size());
            }

        } catch (Exception e) {
            log.error("Error syncing candidate {} to Weaviate: {}", candidateId, e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public void syncAllCandidatesToWeaviate() {
        try {
            // Ensure schema exists
            ensureWeaviateSchema();

            List<Candidate> candidates = candidateRepo.findAll();
            log.info("Starting sync of {} candidates to Weaviate...", candidates.size());

            int successCount = 0;
            int failCount = 0;

            for (Candidate candidate : candidates) {
                try {
                    syncCandidateToWeaviate(candidate.getCandidateId());
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to sync candidate {}: {}", candidate.getCandidateId(), e.getMessage());
                    failCount++;
                }
            }

            log.info("✅ Sync completed: {} succeeded, {} failed", successCount, failCount);

        } catch (Exception e) {
            log.error("Error during batch sync: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public void deleteCandidateFromWeaviate(int candidateId) {
        try {
            String uuid = UUID.nameUUIDFromBytes(String.valueOf(candidateId).getBytes()).toString();

            Result<Boolean> result = weaviateClient.data().deleter()
                    .withClassName(CANDIDATE_CLASS)
                    .withID(uuid)
                    .run();

            if (result.hasErrors()) {
                log.error("Failed to delete candidate {} from Weaviate: {}",
                        candidateId, result.getError().getMessages());
            } else {
                log.info("✅ Deleted candidate {} from Weaviate", candidateId);
            }

        } catch (Exception e) {
            log.error("Error deleting candidate {} from Weaviate: {}", candidateId, e.getMessage(), e);
        }
    }

    private void ensureWeaviateSchema() {
        try {
            // Check if class exists
            Result<Boolean> exists = weaviateClient.schema().exists()
                    .withClassName(CANDIDATE_CLASS)
                    .run();

            if (!exists.getResult()) {
                log.info("Creating Weaviate schema for {}", CANDIDATE_CLASS);

                // Use proper Weaviate schema builder with WeaviateClass
                io.weaviate.client.v1.schema.model.WeaviateClass weaviateClass =
                        io.weaviate.client.v1.schema.model.WeaviateClass.builder()
                        .className(CANDIDATE_CLASS)
                        .description("Candidate profiles with skills and experience")
                        .vectorizer("none") // No vectorizer - using manual skill matching
                        .properties(Arrays.asList(
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("candidateId")
                                        .dataType(Arrays.asList("int"))
                                        .description("Unique candidate identifier")
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("candidateName")
                                        .dataType(Arrays.asList("text"))
                                        .description("Candidate full name")
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("email")
                                        .dataType(Arrays.asList("text"))
                                        .description("Candidate email address")
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("skills")
                                        .dataType(Arrays.asList("text[]"))
                                        .description("List of candidate skills")
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("totalExperience")
                                        .dataType(Arrays.asList("int"))
                                        .description("Total years of experience")
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("aboutMe")
                                        .dataType(Arrays.asList("text"))
                                        .description("Candidate profile summary")
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("syncedAt")
                                        .dataType(Arrays.asList("text"))
                                        .description("Last sync timestamp")
                                        .build()
                        ))
                        .build();

                Result<Boolean> createResult = weaviateClient.schema().classCreator()
                        .withClass(weaviateClass)
                        .run();

                if (createResult.hasErrors()) {
                    log.error("Failed to create schema: {}", createResult.getError().getMessages());
                } else {
                    log.info("✅ Schema created successfully");
                }
            }
        } catch (Exception e) {
            log.error("Error ensuring Weaviate schema: {}", e.getMessage(), e);
        }
    }
}

