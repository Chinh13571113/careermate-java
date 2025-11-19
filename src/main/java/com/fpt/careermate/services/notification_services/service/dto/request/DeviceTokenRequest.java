package com.fpt.careermate.services.notification_services.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for registering a device token for push notifications
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceTokenRequest {

    /**
     * FCM device token from mobile app
     */
    @NotBlank(message = "Device token is required")
    String token;

    /**
     * Device type: IOS or ANDROID
     */
    String deviceType;

    /**
     * Human-readable device name (optional)
     */
    String deviceName;

    /**
     * App version (optional)
     */
    String appVersion;

    /**
     * OS version (optional)
     */
    String osVersion;
}
