package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.BlogCommentService;
import com.fpt.careermate.services.dto.request.BlogCommentRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.BlogCommentResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blogs/{blogId}/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogCommentController {
    BlogCommentService blogCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BlogCommentResponse> createComment(
            @PathVariable Long blogId,
            @Valid @RequestBody BlogCommentRequest request) {
        log.info("REST request to create comment for blog ID: {}", blogId);
        return ApiResponse.<BlogCommentResponse>builder()
                .result(blogCommentService.createComment(blogId, request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<BlogCommentResponse>> getCommentsByBlogId(
            @PathVariable Long blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("REST request to get comments for blog ID: {}", blogId);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<BlogCommentResponse>>builder()
                .result(blogCommentService.getCommentsByBlogId(blogId, pageable))
                .build();
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<BlogCommentResponse> updateComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @Valid @RequestBody BlogCommentRequest request) {
        log.info("REST request to update comment ID: {}", commentId);
        return ApiResponse.<BlogCommentResponse>builder()
                .result(blogCommentService.updateComment(commentId, request))
                .build();
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId) {
        log.info("REST request to delete comment ID: {}", commentId);
        blogCommentService.deleteComment(commentId);
        return ApiResponse.<Void>builder()
                .message("Comment deleted successfully")
                .build();
    }
}

