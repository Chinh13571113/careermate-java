package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingCreationRequest {

    @Size(max = 100)
    @NotBlank
    String title;

    @Size(max = 5000)
    @NotBlank
    String description;

    @NotBlank
    String address;

    @NotNull
    LocalDate expirationDate;

    @NotNull
    Set<JdSkillRequest> jdSkills;

}
