package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.BlogRatingService;
import com.fpt.careermate.services.dto.request.BlogRatingRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.BlogRatingResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blogs/{blogId}/ratings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogRatingController {
    BlogRatingService blogRatingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BlogRatingResponse> rateBlog(
            @PathVariable Long blogId,
            @Valid @RequestBody BlogRatingRequest request) {
        log.info("REST request to rate blog ID: {}", blogId);
        return ApiResponse.<BlogRatingResponse>builder()
                .result(blogRatingService.rateBlog(blogId, request))
                .build();
    }

    @GetMapping("/my-rating")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BlogRatingResponse> getUserRating(@PathVariable Long blogId) {
        log.info("REST request to get user rating for blog ID: {}", blogId);
        return ApiResponse.<BlogRatingResponse>builder()
                .result(blogRatingService.getUserRating(blogId))
                .build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteRating(@PathVariable Long blogId) {
        log.info("REST request to delete rating for blog ID: {}", blogId);
        blogRatingService.deleteRating(blogId);
        return ApiResponse.<Void>builder()
                .message("Rating deleted successfully")
                .build();
    }
}

