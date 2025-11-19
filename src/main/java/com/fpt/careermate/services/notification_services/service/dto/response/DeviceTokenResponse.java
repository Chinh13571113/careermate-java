package com.fpt.careermate.services.notification_services.service.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Response DTO for device token information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceTokenResponse {

    Long id;
    String userId;
    String deviceType;
    String deviceName;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime lastUsedAt;
    String appVersion;
    String osVersion;
}
