package com.fpt.careermate.services.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogRatingResponse {
    Long id;
    Long blogId;
    Long userId;
    Integer rating;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

