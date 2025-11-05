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
import com.google.gson.GsonBuilder;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.schema.model.WeaviateClass;
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
    com.fpt.careermate.services.recommendation.util.SkillMatcher skillMatcher;

    private static final String CANDIDATE_CLASS = "CandidateProfile";
    private static final int DEFAULT_MAX_CANDIDATES = 10;
    private static final double DEFAULT_MIN_MATCH_SCORE = 0.5; // Require at least 50% match for better quality

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
            // Create semantic search query from skills
            String searchQuery = String.join(" ", requiredSkills);
            log.info("🔎 Searching Weaviate with semantic query: '{}' (limit: {})", searchQuery, limit);

            Field[] fields = new Field[]{
                    Field.builder().name("candidateId").build(),
                    Field.builder().name("candidateName").build(),
                    Field.builder().name("email").build(),
                    Field.builder().name("skills").build(),
                    Field.builder().name("totalExperience").build(),
                    Field.builder().name("aboutMe").build(),
                    Field.builder()
                            .name("_additional")
                            .fields(new Field[]{
                                    Field.builder().name("distance").build(),
                                    Field.builder().name("certainty").build()
                            })
                            .build()
            };

            // Use nearText for semantic search with embeddings
            // This uses the configured sentence-transformers model
            // Use lower certainty (0.3) for initial fetch to get more candidates
            // We'll apply the actual threshold after combining with skill matching
            Result<GraphQLResponse> result = weaviateClient.graphQL().get()
                    .withClassName(CANDIDATE_CLASS)
                    .withNearText(weaviateClient.graphQL().arguments().nearTextArgBuilder()
                            .concepts(new String[]{searchQuery})
                            .certainty(0.3f) // Lower threshold for initial fetch
                            .build())
                    .withLimit(limit * 3) // Fetch more candidates to filter and rank
                    .withFields(fields)
                    .run();

            if (result.hasErrors()) {
                log.error("❌ Weaviate semantic search error: {}", result.getError().getMessages());
                return Collections.emptyList();
            }

            log.info("✅ Weaviate semantic search completed, parsing results...");
            // Parse and rank results
            List<CandidateRecommendationDTO> recommendations = parseSemanticSearchResults(
                    result.getResult(), requiredSkills, minYearsExperience, limit, threshold);
            log.info("📈 Found {} matching candidates", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("❌ Error in semantic search: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<CandidateRecommendationDTO> parseSemanticSearchResults(
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

            log.info("📦 Processing {} candidates from semantic search", candidates.size());

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

                    // Extract semantic similarity scores
                    double semanticScore = 0.0;
                    Object additionalObj = candidate.get("_additional");
                    if (additionalObj instanceof Map) {
                        Map<String, Object> additional = (Map<String, Object>) additionalObj;
                        Object certaintyObj = additional.get("certainty");
                        if (certaintyObj != null) {
                            semanticScore = ((Number) certaintyObj).doubleValue();
                        }
                    }

                    // Use SkillMatcher for additional skill analysis
                    Set<String> matchedSkillsSet = skillMatcher.findMatchingSkills(requiredSkills, candidateSkills);
                    List<String> matchedSkills = new ArrayList<>(matchedSkillsSet);

                    Set<String> missingSkillsSet = skillMatcher.findMissingSkills(requiredSkills, candidateSkills);
                    List<String> missingSkills = new ArrayList<>(missingSkillsSet);

                    // Calculate exact skill matching score (more weight)
                    double skillMatchScore = skillMatcher.calculateEnhancedMatchScore(requiredSkills, candidateSkills);

                    // Calculate experience factor (0.8 to 1.2 multiplier based on experience)
                    double experienceFactor = 1.0;
                    if (minYearsExperience > 0) {
                        if (totalExperience >= minYearsExperience) {
                            // Bonus for meeting experience requirement
                            experienceFactor = Math.min(1.2, 1.0 + (totalExperience - minYearsExperience) * 0.02);
                        } else {
                            // Penalty for not meeting experience requirement
                            experienceFactor = 0.8 + (totalExperience / (double) minYearsExperience) * 0.2;
                        }
                    }

                    // Improved scoring: 50% exact skill match, 40% semantic similarity, 10% experience
                    // This prioritizes candidates with actual matching skills
                    double baseScore = (skillMatchScore * 0.5) + (semanticScore * 0.4);
                    double combinedScore = baseScore * experienceFactor;

                    // Cap the combined score at 1.0
                    combinedScore = Math.min(1.0, combinedScore);

                    log.debug("✅ Candidate {} - Semantic: {}, Skill: {}, Exp Factor: {}, Combined: {}",
                            candidateId,
                            String.format("%.2f", semanticScore),
                            String.format("%.2f", skillMatchScore),
                            String.format("%.2f", experienceFactor),
                            String.format("%.2f", combinedScore));

                    // Build recommendation DTO
                    CandidateRecommendationDTO recommendation = CandidateRecommendationDTO.builder()
                            .candidateId(candidateId)
                            .candidateName(candidateName)
                            .email(email)
                            .matchScore(combinedScore)
                            .matchedSkills(matchedSkills)
                            .missingSkills(missingSkills)
                            .totalYearsExperience(totalExperience)
                            .profileSummary(aboutMe)
                            .build();

                    recommendations.add(recommendation);

                } catch (Exception e) {
                    log.warn("⚠️ Error parsing semantic search result: {}", e.getMessage());
                }
            }

            // Filter by threshold first (use the threshold from parameters)
            List<CandidateRecommendationDTO> filtered = recommendations.stream()
                    .filter(rec -> rec.getMatchScore() >= threshold)
                    .collect(Collectors.toList());

            log.info("🎯 Filtered from {} to {} candidates meeting threshold {}",
                    recommendations.size(), filtered.size(), String.format("%.2f", threshold));

            // Sort by combined score (descending) and then by years of experience
            filtered.sort((a, b) -> {
                int scoreCompare = Double.compare(b.getMatchScore(), a.getMatchScore());
                if (scoreCompare != 0) return scoreCompare;
                return Integer.compare(b.getTotalYearsExperience(), a.getTotalYearsExperience());
            });

            // Return top N results
            return filtered.stream()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error parsing semantic search results: {}", e.getMessage(), e);
        }

        return recommendations;
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

                    // Use SkillMatcher for intelligent skill matching with synonyms and hierarchy
                    Set<String> matchedSkillsSet = skillMatcher.findMatchingSkills(requiredSkills, candidateSkills);
                    List<String> matchedSkills = new ArrayList<>(matchedSkillsSet);

                    Set<String> missingSkillsSet = skillMatcher.findMissingSkills(requiredSkills, candidateSkills);
                    List<String> missingSkills = new ArrayList<>(missingSkillsSet);

                    // Calculate enhanced match score with synonym matching and hierarchy bonus
                    double matchScore = skillMatcher.calculateEnhancedMatchScore(requiredSkills, candidateSkills);


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

                // Configure Weaviate Embeddings Inference API
                // This uses Weaviate's built-in embedding service
                Map<String, Object> moduleConfig = new HashMap<>();
                Map<String, Object> text2vecWeaviate = new HashMap<>();
                text2vecWeaviate.put("vectorizeClassName", false);
                // DON'T specify model - let Weaviate Cloud use its default model
                // The model naming in Weaviate Cloud API is inconsistent/undocumented
                // Default model is sufficient for semantic matching
                moduleConfig.put("text2vec-weaviate", text2vecWeaviate);

                // Configure property-level vectorization settings
                Map<String, Object> skillsModuleConfig = new HashMap<>();
                Map<String, Object> skillsText2vec = new HashMap<>();
                skillsText2vec.put("skip", false);
                skillsText2vec.put("vectorizePropertyName", false);
                skillsModuleConfig.put("text2vec-weaviate", skillsText2vec);

                Map<String, Object> aboutMeModuleConfig = new HashMap<>();
                Map<String, Object> aboutMeText2vec = new HashMap<>();
                aboutMeText2vec.put("skip", false);
                aboutMeText2vec.put("vectorizePropertyName", false);
                aboutMeModuleConfig.put("text2vec-weaviate", aboutMeText2vec);

                Map<String, Object> skipModuleConfig = new HashMap<>();
                Map<String, Object> skipText2vec = new HashMap<>();
                skipText2vec.put("skip", true);
                skipText2vec.put("vectorizePropertyName", false);
                skipModuleConfig.put("text2vec-weaviate", skipText2vec);

                io.weaviate.client.v1.schema.model.WeaviateClass weaviateClass =
                        io.weaviate.client.v1.schema.model.WeaviateClass.builder()
                        .className(CANDIDATE_CLASS)
                        .description("Candidate profiles with skills and experience for semantic matching")
                        .vectorizer("text2vec-weaviate") // Use Weaviate's embedding inference API
                        .moduleConfig(moduleConfig)
                        .properties(Arrays.asList(
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("candidateId")
                                        .dataType(Arrays.asList("int"))
                                        .description("Unique candidate identifier")
                                        .moduleConfig(skipModuleConfig)
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("candidateName")
                                        .dataType(Arrays.asList("text"))
                                        .description("Candidate full name")
                                        .moduleConfig(skipModuleConfig)
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("email")
                                        .dataType(Arrays.asList("text"))
                                        .description("Candidate email address")
                                        .moduleConfig(skipModuleConfig)
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("skills")
                                        .dataType(Arrays.asList("text[]"))
                                        .description("List of candidate skills - vectorized for semantic search")
                                        .moduleConfig(skillsModuleConfig)
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("totalExperience")
                                        .dataType(Arrays.asList("int"))
                                        .description("Total years of experience")
                                        .moduleConfig(skipModuleConfig)
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("aboutMe")
                                        .dataType(Arrays.asList("text"))
                                        .description("Candidate profile summary - vectorized for semantic search")
                                        .moduleConfig(aboutMeModuleConfig)
                                        .build(),
                                io.weaviate.client.v1.schema.model.Property.builder()
                                        .name("syncedAt")
                                        .dataType(Arrays.asList("text"))
                                        .description("Last sync timestamp")
                                        .moduleConfig(skipModuleConfig)
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

    public void getCollection() {
        Result<WeaviateClass> result = weaviateClient.schema().classGetter()
                .withClassName(CANDIDATE_CLASS)
                .run();

        String json = new GsonBuilder().setPrettyPrinting().create().toJson(result.getResult());
        log.info("json: {}", json);
    }

    @Override
    public void recreateSchema() {
        try {
            log.info("🔄 Recreating Weaviate schema...");

            // Delete existing schema if it exists
            Result<Boolean> deleteResult = weaviateClient.schema().classDeleter()
                    .withClassName(CANDIDATE_CLASS)
                    .run();

            if (deleteResult.hasErrors()) {
                log.warn("⚠️ Could not delete existing schema (might not exist): {}",
                        deleteResult.getError().getMessages());
            } else {
                log.info("🗑️ Deleted existing schema");
            }

            // Wait a bit for deletion to complete
            Thread.sleep(2000);

            // Create new schema with proper vectorization
            ensureWeaviateSchema();

            log.info("✅ Schema recreated successfully. Please re-sync candidates.");

        } catch (Exception e) {
            log.error("❌ Error recreating schema: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to recreate schema", e);
        }
    }
}

