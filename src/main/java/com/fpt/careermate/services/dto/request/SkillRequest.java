package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillRequest {
    @NotNull(message = "Resume ID is required")
    Integer resumeId;

    @NotBlank(message = "Skill type is required")
    String skillType;
    @NotBlank(message = "Skill name is required")
    String skillName;
    Integer yearOfExperience;
}
