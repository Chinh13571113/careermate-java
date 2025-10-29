package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.job_services.service.WeaviateImp;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/weaviate")
@Tag(name = "Weaviate", description = "Manage job posting")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WeaviateController {

    WeaviateImp weaviateImp;

    @PostMapping
    ApiResponse<String> createJobPosting() {
        weaviateImp.createJobPostingCollection();
        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();
    }

    @DeleteMapping("/collection")
    ApiResponse<String> deleteCollection() {
        weaviateImp.deleteJobPostingCollection();
        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/object")
    ApiResponse<String> getJobPosting() {
        weaviateImp.getJobPostings();
        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/collection")
    ApiResponse<String> getCollectionList() {
        weaviateImp.getAllCollections();
        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();
    }

}
