package com.fpt.careermate.services.recommendation.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.recommendation.dto.RecommendationResponseDTO;
import com.fpt.careermate.services.recommendation.service.CandidateRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Candidate Recommendation", description = "AI-powered candidate recommendation system")
public class CandidateRecommendationController {

    CandidateRecommendationService recommendationService;

    @GetMapping("/recruiter/recommendations/job/{jobPostingId}")
    @PreAuthorize("hasRole('RECRUITER')")
    @SecurityRequirement(name = "bearerToken")
    @Operation(
            summary = "Get recommended candidates for a job posting",
            description = "Uses AI to find and rank candidates whose skills match the job requirements"
    )
    public ApiResponse<RecommendationResponseDTO> getRecommendedCandidates(
            @Parameter(description = "Job posting ID") @PathVariable int jobPostingId,
            @Parameter(description = "Maximum number of candidates to return") @RequestParam(required = false) Integer maxCandidates,
            @Parameter(description = "Minimum match score (0.0 - 1.0)") @RequestParam(required = false) Double minMatchScore
    ) {
        log.info("🔍 Getting recommended candidates for job posting ID: {} (maxCandidates: {}, minScore: {})",
                jobPostingId, maxCandidates, minMatchScore);
        RecommendationResponseDTO response = recommendationService.getRecommendedCandidatesForJob(
                jobPostingId,
                maxCandidates,
                minMatchScore
        );
        log.info("📊 Found {} candidates for job posting {}",
                response.getTotalCandidatesFound(), jobPostingId);
        return ApiResponse.<RecommendationResponseDTO>builder()
                .result(response)
                .build();
    }

    @PostMapping("/admin/recommendations/sync-candidate/{candidateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerToken")
    @Operation(
            summary = "Sync single candidate to Weaviate",
            description = "Syncs a candidate's profile data to the Weaviate vector database for recommendations"
    )
    public ApiResponse<String> syncCandidateToWeaviate(
            @Parameter(description = "Candidate ID") @PathVariable int candidateId
    ) {
        log.info("🔄 Admin syncing candidate {} to Weaviate", candidateId);
        try {
            recommendationService.syncCandidateToWeaviate(candidateId);
            log.info("✅ Successfully synced candidate {} to Weaviate", candidateId);
            return ApiResponse.<String>builder()
                    .result("Candidate synced successfully to recommendation system")
                    .build();
        } catch (Exception e) {
            log.error("❌ Failed to sync candidate {}: {}", candidateId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/admin/recommendations/sync-all-candidates")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerToken")
    @Operation(
            summary = "Sync all candidates to Weaviate",
            description = "Batch syncs all candidate profiles to Weaviate for the recommendation system"
    )
    public ApiResponse<String> syncAllCandidatesToWeaviate() {
        log.info("Admin syncing all candidates to Weaviate");
        recommendationService.syncAllCandidatesToWeaviate();
        return ApiResponse.<String>builder()
                .result("All candidates sync started")
                .build();
    }

    @DeleteMapping("/admin/recommendations/candidate/{candidateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete candidate from Weaviate",
            description = "Removes a candidate's profile from the Weaviate recommendation index"
    )
    public ApiResponse<String> deleteCandidateFromWeaviate(
            @Parameter(description = "Candidate ID") @PathVariable int candidateId
    ) {
        log.info("Admin deleting candidate {} from Weaviate", candidateId);
        recommendationService.deleteCandidateFromWeaviate(candidateId);
        return ApiResponse.<String>builder()
                .result("Candidate deleted from recommendation system")
                .build();
    }
}

