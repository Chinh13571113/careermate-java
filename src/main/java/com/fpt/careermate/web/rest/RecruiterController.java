package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.RecruiterImp;
import com.fpt.careermate.services.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/recruiter")
@Tag(name = "Recruiter", description = "Manage recruiter")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterController {

    RecruiterImp recruiterImp;

    @Operation(summary = "Create recruiter")
    @PostMapping
    public ApiResponse<NewRecruiterResponse> createRecruiter(@Valid @RequestBody RecruiterCreationRequest request) {
        return ApiResponse.<NewRecruiterResponse>builder()
                .result(recruiterImp.createRecruiter(request))
                .code(200)
                .message("success")
                .build();
    }

}
