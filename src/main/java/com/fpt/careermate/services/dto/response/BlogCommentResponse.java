package com.fpt.careermate.services.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogCommentResponse {
    Long id;
    Long blogId;
    Long userId;
    String userName;
    String content;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

