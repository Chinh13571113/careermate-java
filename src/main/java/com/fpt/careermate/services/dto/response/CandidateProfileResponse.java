package com.fpt.careermate.services.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class CandidateProfileResponse {
    LocalDateTime dob;
    String title;
    String phone;
    String address;
    String image;
    String gender;
    String link;
}
