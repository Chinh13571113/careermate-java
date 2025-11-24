package com.fpt.careermate.services.notification_services.service.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BroadcastNotificationRequest {
    String title;
    String message;
    String priority; // "HIGH", "MEDIUM", "LOW"
    String targetRole; // Optional: "CANDIDATE", "RECRUITER", "ADMIN", or null for all
}
