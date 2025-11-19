package com.fpt.careermate.services.notification_services.web.rest;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.notification_services.domain.DeviceToken;
import com.fpt.careermate.services.notification_services.repository.DeviceTokenRepo;
import com.fpt.careermate.services.notification_services.service.dto.request.DeviceTokenRequest;
import com.fpt.careermate.services.notification_services.service.dto.response.DeviceTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing device tokens for push notifications.
 * Mobile apps use these endpoints to register/unregister FCM tokens.
 */
@RestController
@RequestMapping("/api/device-tokens")
@Tag(name = "Device Tokens", description = "Manage device tokens for push notifications")
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenController {

    private final DeviceTokenRepo deviceTokenRepo;

    /**
     * Register a device token for push notifications.
     * If token already exists, it will be reactivated and updated.
     *
     * **Mobile Integration:**
     * 
     * React Native (with @react-native-firebase/messaging):
     * ```javascript
     * import messaging from '@react-native-firebase/messaging';
     * 
     * // Request permission
     * const authStatus = await messaging().requestPermission();
     * 
     * if (authStatus === messaging.AuthorizationStatus.AUTHORIZED) {
     * // Get FCM token
     * const token = await messaging().getToken();
     * 
     * // Register with backend
     * await axios.post('/api/device-tokens/register', {
     * token,
     * deviceType: Platform.OS === 'ios' ? 'IOS' : 'ANDROID',
     * deviceName: await DeviceInfo.getDeviceName(),
     * appVersion: DeviceInfo.getVersion(),
     * osVersion: DeviceInfo.getSystemVersion()
     * });
     * }
     * ```
     * 
     * Flutter (with firebase_messaging):
     * ```dart
     * import 'package:firebase_messaging/firebase_messaging.dart';
     * 
     * // Get FCM token
     * String? token = await FirebaseMessaging.instance.getToken();
     * 
     * // Register with backend
     * await dio.post('/api/device-tokens/register', data: {
     * 'token': token,
     * 'deviceType': Platform.isIOS ? 'IOS' : 'ANDROID',
     * 'deviceName': await DeviceInfoPlugin().model,
     * 'appVersion': packageInfo.version,
     * 'osVersion': await DeviceInfoPlugin().version
     * });
     * ```
     *
     * @param request Device token registration details
     * @return Success response
     */
    @PostMapping("/register")
    @Operation(summary = "Register device token", description = """
            Register an FCM device token to receive push notifications.

            **When to call:**
            - When app starts (if user is logged in)
            - After user logs in
            - When FCM token refreshes

            **Behavior:**
            - If token already exists → reactivates and updates metadata
            - If token is new → creates new registration
            - One user can have multiple tokens (multiple devices)

            **Required:** JWT authentication

            **Request Body Example:**
            ```json
            {
              "token": "fPGcneovSCmgBy2hDobo4r:APA91bHdfE91...",
              "deviceType": "ANDROID",
              "deviceName": "OPPO A9 2020",
              "appVersion": "1.0.0",
              "osVersion": "11"
            }
            ```
            """)
    public ApiResponse<DeviceTokenResponse> registerDeviceToken(@RequestBody @Valid DeviceTokenRequest request) {
        try {
            String userId = getCurrentUserId();

            log.info("📱 Registering device token | userId: {} | deviceType: {} | token: {}...",
                    userId, request.getDeviceType(),
                    request.getToken() != null
                            ? request.getToken().substring(0, Math.min(20, request.getToken().length()))
                            : "null");

            // Validate token is not null or empty
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                log.error("❌ Device token is null or empty");
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            // Check if token already exists
            DeviceToken deviceToken = deviceTokenRepo.findByToken(request.getToken())
                    .orElse(null);

            if (deviceToken != null) {
                // Token exists - update it
                deviceToken.setUserId(userId); // Update user if changed
                deviceToken.setIsActive(true);
                deviceToken.setDeviceType(request.getDeviceType());
                deviceToken.setDeviceName(request.getDeviceName());
                deviceToken.setAppVersion(request.getAppVersion());
                deviceToken.setOsVersion(request.getOsVersion());
                deviceToken.setLastUsedAt(LocalDateTime.now());

                log.info("🔄 Updated existing device token | userId: {} | tokenId: {}", userId, deviceToken.getId());
            } else {
                // New token - create it
                deviceToken = DeviceToken.builder()
                        .userId(userId)
                        .token(request.getToken())
                        .deviceType(request.getDeviceType())
                        .deviceName(request.getDeviceName())
                        .appVersion(request.getAppVersion())
                        .osVersion(request.getOsVersion())
                        .isActive(true)
                        .createdAt(LocalDateTime.now())
                        .lastUsedAt(LocalDateTime.now())
                        .build();

                log.info("✅ Registered new device token | userId: {} | deviceType: {}", userId,
                        request.getDeviceType());
            }

            deviceToken = deviceTokenRepo.save(deviceToken);

            DeviceTokenResponse response = mapToResponse(deviceToken);

            return ApiResponse.<DeviceTokenResponse>builder()
                    .result(response)
                    .message("Device token registered successfully")
                    .build();

        } catch (AppException e) {
            log.error("❌ AppException during device token registration: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error during device token registration", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /**
     * Unregister a device token (e.g., on logout).
     *
     * @param token The FCM token to unregister
     * @return Success response
     */
    @DeleteMapping("/unregister")
    @Operation(summary = "Unregister device token", description = """
            Unregister an FCM device token to stop receiving push notifications.

            **When to call:**
            - When user logs out
            - When user disables notifications in settings
            - When app is uninstalled (optional cleanup)

            **Behavior:**
            - Marks token as inactive (doesn't delete it immediately)
            - Token can be reactivated by registering again
            """)
    public ApiResponse<Void> unregisterDeviceToken(@RequestParam String token) {
        deviceTokenRepo.findByToken(token).ifPresent(deviceToken -> {
            deviceToken.setIsActive(false);
            deviceTokenRepo.save(deviceToken);
            log.info("🔕 Unregistered device token | userId: {} | deviceType: {}",
                    deviceToken.getUserId(), deviceToken.getDeviceType());
        });

        return ApiResponse.<Void>builder()
                .message("Device token unregistered successfully")
                .build();
    }

    /**
     * Get all device tokens for the current user.
     *
     * @return List of device tokens
     */
    @GetMapping("/my-devices")
    @Operation(summary = "Get my registered devices", description = """
            Get all device tokens registered for the current user.
            Useful for showing user which devices are receiving notifications.
            """)
    public ApiResponse<List<DeviceTokenResponse>> getMyDevices() {
        String userId = getCurrentUserId();

        List<DeviceToken> tokens = deviceTokenRepo.findByUserId(userId);

        List<DeviceTokenResponse> responses = tokens.stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DeviceTokenResponse>>builder()
                .result(responses)
                .message(String.format("Found %d device(s)", responses.size()))
                .build();
    }

    /**
     * Delete all inactive device tokens for current user.
     *
     * @return Success response
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "Clean up inactive devices", description = "Remove all inactive device tokens for the current user")
    public ApiResponse<Void> cleanupInactiveDevices() {
        String userId = getCurrentUserId();

        List<DeviceToken> inactiveTokens = deviceTokenRepo.findByUserId(userId).stream()
                .filter(token -> !token.getIsActive())
                .toList();

        deviceTokenRepo.deleteAll(inactiveTokens);

        log.info("🧹 Cleaned up {} inactive tokens for user: {}", inactiveTokens.size(), userId);

        return ApiResponse.<Void>builder()
                .message(String.format("Removed %d inactive device(s)", inactiveTokens.size()))
                .build();
    }

    /**
     * Map DeviceToken entity to response DTO.
     */
    private DeviceTokenResponse mapToResponse(DeviceToken token) {
        return DeviceTokenResponse.builder()
                .id(token.getId())
                .userId(token.getUserId())
                .deviceType(token.getDeviceType())
                .deviceName(token.getDeviceName())
                .isActive(token.getIsActive())
                .createdAt(token.getCreatedAt())
                .lastUsedAt(token.getLastUsedAt())
                .appVersion(token.getAppVersion())
                .osVersion(token.getOsVersion())
                .build();
    }

    /**
     * Get current authenticated user ID from SecurityContext.
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
