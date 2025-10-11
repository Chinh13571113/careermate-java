package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.JdJdSkillImp;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.JdSkillResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/jdskill")
@Tag(name = "JdSkill", description = "Manage jdSkill")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JdSkillController {

    JdJdSkillImp jdSkillImp;

    @PostMapping
    @Operation(summary = "Create jdSkill")
    ApiResponse<String> createUser(
            @RequestParam
            @NotBlank
            String name
    ) {
        jdSkillImp.createSkill(name);
        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping
    @Operation(summary = "Get jdSkill list")
    ApiResponse<List<JdSkillResponse>> getSkillList() {
        return ApiResponse.<List<JdSkillResponse>>builder()
                .result(jdSkillImp.getAllSkill())
                .code(200)
                .message("success")
                .build();
    }

}
