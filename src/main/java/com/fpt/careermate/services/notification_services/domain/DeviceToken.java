package com.fpt.careermate.services.notification_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Entity representing a mobile device token for push notifications.
 * Stores FCM (Firebase Cloud Messaging) tokens for sending push notifications
 * to mobile devices.
 * Each user can have multiple device tokens (multiple devices or apps).
 */
@Entity
@Table(name = "device_tokens", indexes = {
        @Index(name = "idx_device_user_active", columnList = "user_id, is_active"),
        @Index(name = "idx_device_token", columnList = "token")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    /**
     * User ID (email) who owns this device token
     */
    @Column(name = "user_id", nullable = false, length = 255)
    String userId;

    /**
     * FCM device token for push notifications
     */
    @Column(name = "token", nullable = false, unique = true, length = 512)
    String token;

    /**
     * Device type: IOS or ANDROID
     */
    @Column(name = "device_type", length = 20)
    String deviceType;

    /**
     * Human-readable device name (e.g., "iPhone 15 Pro", "Samsung Galaxy S24")
     */
    @Column(name = "device_name", length = 255)
    String deviceName;

    /**
     * Whether this token is still active and should receive notifications
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    /**
     * When the device token was first registered
     */
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Last time this token was used successfully to send a notification
     */
    @Column(name = "last_used_at")
    LocalDateTime lastUsedAt;

    /**
     * App version for tracking
     */
    @Column(name = "app_version", length = 50)
    String appVersion;

    /**
     * OS version for tracking
     */
    @Column(name = "os_version", length = 50)
    String osVersion;
}
