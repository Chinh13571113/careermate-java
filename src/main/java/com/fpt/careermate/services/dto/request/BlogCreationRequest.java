package com.fpt.careermate.services.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogCreationRequest {
    String title;
    String content;
    String summary;
    String thumbnailUrl;
    String category;
    List<String> tags;
    String status;
}
