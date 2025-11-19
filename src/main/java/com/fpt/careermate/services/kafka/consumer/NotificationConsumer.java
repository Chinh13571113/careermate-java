package com.fpt.careermate.services.kafka.consumer;

import com.fpt.careermate.common.util.MailBody;
import com.fpt.careermate.config.KafkaConfig;
import com.fpt.careermate.services.email_services.service.impl.EmailService;
import com.fpt.careermate.services.health_services.domain.NotificationHeartbeat;
import com.fpt.careermate.services.health_services.repository.NotificationHeartbeatRepo;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.notification_services.domain.Notification;
import com.fpt.careermate.services.notification_services.repository.NotificationRepo;
import com.fpt.careermate.services.notification_services.service.FcmPushNotificationService;
import com.fpt.careermate.services.notification_services.service.NotificationSseService;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka consumer service for processing notifications
 * Processes notification events from Kafka topics, stores them in database,
 * and sends email notifications when appropriate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationHeartbeatRepo heartbeatRepo;
    private final NotificationRepo notificationRepo;
    private final EmailService emailService;
    private final NotificationSseService sseService;
    private final FcmPushNotificationService fcmService;

    /**
     * Consumer for admin notifications
     */
    @KafkaListener(topics = KafkaConfig.ADMIN_NOTIFICATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeAdminNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("📨 Received ADMIN notification | Topic: {} | Partition: {} | Offset: {} | EventId: {}",
                    KafkaConfig.ADMIN_NOTIFICATION_TOPIC, partition, offset, event.getEventId());

            // Process the notification
            processNotification(event);

            // Manually commit offset after successful processing
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed ADMIN notification: {}", event.getEventId());

            // Update heartbeat
            updateHeartbeat(true, null);

        } catch (Exception e) {
            log.error("❌ Error processing ADMIN notification: {} | Error: {}",
                    event.getEventId(), e.getMessage(), e);
            // Update heartbeat with error
            updateHeartbeat(false, e.getMessage());
            // Don't acknowledge - message will be reprocessed
        }
    }

    /**
     * Consumer for recruiter notifications
     */
    @KafkaListener(topics = KafkaConfig.RECRUITER_NOTIFICATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeRecruiterNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("📨 Received RECRUITER notification | Topic: {} | Partition: {} | Offset: {} | EventId: {}",
                    KafkaConfig.RECRUITER_NOTIFICATION_TOPIC, partition, offset, event.getEventId());

            // Process the notification
            processNotification(event);

            // Manually commit offset after successful processing
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed RECRUITER notification: {}", event.getEventId());

            // Update heartbeat
            updateHeartbeat(true, null);

        } catch (Exception e) {
            log.error("❌ Error processing RECRUITER notification: {} | Error: {}",
                    event.getEventId(), e.getMessage(), e);
            // Update heartbeat with error
            updateHeartbeat(false, e.getMessage());
            // Don't acknowledge - message will be reprocessed
        }
    }

    /**
     * Consumer for candidate notifications
     */
    @KafkaListener(topics = KafkaConfig.CANDIDATE_NOTIFICATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeCandidateNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("📨 Received CANDIDATE notification | Topic: {} | Partition: {} | Offset: {} | EventId: {}",
                    KafkaConfig.CANDIDATE_NOTIFICATION_TOPIC, partition, offset, event.getEventId());

            // Process the notification
            processNotification(event);

            // Manually commit offset after successful processing
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed CANDIDATE notification: {}", event.getEventId());

            // Update heartbeat
            updateHeartbeat(true, null);

        } catch (Exception e) {
            log.error("❌ Error processing CANDIDATE notification: {} | Error: {}",
                    event.getEventId(), e.getMessage(), e);
            // Update heartbeat with error
            updateHeartbeat(false, e.getMessage());
            // Don't acknowledge - message will be reprocessed
        }
    }

    /**
     * Process notification event
     * Save to database for REST API retrieval by mobile clients
     */
    private void processNotification(NotificationEvent event) {
        log.info("Processing notification: {}", event);

        try {
            // Check if notification already exists (idempotency)
            if (notificationRepo.existsByEventId(event.getEventId())) {
                log.warn("⚠️ Notification with eventId {} already exists, skipping", event.getEventId());
                return;
            }

            // Save notification to database
            Notification notification = Notification.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .recipientId(event.getRecipientId())
                    .recipientEmail(event.getRecipientEmail())
                    .title(event.getTitle())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .category(event.getCategory())
                    .metadata(event.getMetadata())
                    .priority(event.getPriority())
                    .isRead(false)
                    .createdAt(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                    .build();

            notificationRepo.save(notification);
            log.info("✅ Notification saved to database | eventId: {} | recipient: {}",
                    event.getEventId(), event.getRecipientId());

            // Send real-time notification via SSE
            sendSseNotification(notification);

            // Send push notification to mobile devices
            sendPushNotification(notification);

            // Send email notifications for important events
            sendAdditionalNotifications(event);

        } catch (Exception e) {
            log.error("❌ Failed to save notification to database | eventId: {}", event.getEventId(), e);
            throw e; // Re-throw to trigger Kafka retry
        }
    }

    /**
     * Send additional notifications (email, push, SMS, etc.)
     * This can be extended based on requirements
     */
    private void sendAdditionalNotifications(NotificationEvent event) {
        // Log different event types for monitoring
        switch (event.getEventType()) {
            case "JOB_POSTING_APPROVED":
                log.info("📋 Job posting approved notification processed for: {}", event.getRecipientId());
                break;
            case "JOB_POSTING_REJECTED":
                log.info("❌ Job posting rejected notification processed for: {}", event.getRecipientId());
                break;
            case "APPLICATION_RECEIVED":
                log.info("📬 Application received notification processed for: {}", event.getRecipientId());
                break;
            case "APPLICATION_STATUS_CHANGED":
                log.info("🔄 Application status changed notification processed for: {}", event.getRecipientId());
                break;
            case "PROFILE_UPDATE_REQUEST":
                log.info("👤 Profile update request notification processed for admin");
                break;
            case "PROFILE_UPDATE_APPROVED":
                log.info("✅ Profile update approved notification processed for: {}", event.getRecipientId());
                break;
            case "PROFILE_UPDATE_REJECTED":
                log.info("❌ Profile update rejected notification processed for: {}", event.getRecipientId());
                break;
            case "DAILY_REMINDER":
                log.info("⏰ Daily reminder notification processed for: {}", event.getRecipientId());
                break;
            case "ANNOUNCEMENT":
                log.info("📢 Announcement notification processed for: {}", event.getRecipientId());
                break;
            default:
                log.info("📧 Generic notification processed for: {}", event.getRecipientId());
        }

        // Send email notification for important events
        if (shouldSendEmail(event)) {
            sendEmailNotification(event);
        }

        // TODO: Future enhancement - Push notifications
        // Example: pushNotificationService.send(event.getRecipientId(),
        // event.getTitle(), event.getMessage());
    }

    /**
     * Send email notification
     * Handles email sending with proper error handling to prevent notification
     * processing failure
     */
    private void sendEmailNotification(NotificationEvent event) {
        try {
            // Validate email address exists
            if (event.getRecipientEmail() == null || event.getRecipientEmail().trim().isEmpty()) {
                log.warn("⚠️ Skipping email for eventId {} - no recipient email", event.getEventId());
                return;
            }

            // Build email body
            MailBody mailBody = MailBody.builder()
                    .to(event.getRecipientEmail())
                    .subject(event.getSubject() != null ? event.getSubject() : event.getTitle())
                    .text(formatEmailMessage(event))
                    .build();

            // Send email
            emailService.sendSimpleEmail(mailBody);

            log.info("✅ Email sent successfully | eventId: {} | recipient: {} | subject: {}",
                    event.getEventId(), event.getRecipientEmail(), mailBody.subject());

        } catch (Exception e) {
            // Log error but don't fail notification processing
            // The notification is already saved in database, so user can still see it
            // in-app
            log.error("❌ Failed to send email | eventId: {} | recipient: {} | error: {}",
                    event.getEventId(), event.getRecipientEmail(), e.getMessage());
            // Don't re-throw - email failure should not trigger Kafka retry
        }
    }

    /**
     * Determine if email should be sent for this notification
     * Sends email for high/medium priority notifications and important event types
     */
    private boolean shouldSendEmail(NotificationEvent event) {
        // Always send email for high priority (priority 1)
        if (event.getPriority() != null && event.getPriority() == 1) {
            return true;
        }

        // Send email for medium priority (priority 2) notifications
        if (event.getPriority() != null && event.getPriority() == 2) {
            return true;
        }

        // Send email for critical event types regardless of priority
        List<String> emailRequiredEvents = List.of(
                "APPLICATION_STATUS_CHANGED", // Candidate needs to know about status changes
                "JOB_POSTING_APPROVED", // Recruiter needs to know their posting is live
                "JOB_POSTING_REJECTED", // Recruiter needs to know and take action
                "PROFILE_UPDATE_APPROVED", // User needs to know their update was approved
                "PROFILE_UPDATE_REJECTED", // User needs to know and possibly resubmit
                "APPLICATION_RECEIVED" // Recruiter should know about new applications
        );

        return emailRequiredEvents.contains(event.getEventType());
    }

    /**
     * Format email message with proper structure
     * Adds header and footer to make emails look professional
     */
    private String formatEmailMessage(NotificationEvent event) {
        StringBuilder formattedMessage = new StringBuilder();

        // Add greeting based on title
        if (event.getTitle() != null && !event.getTitle().isEmpty()) {
            formattedMessage.append(event.getTitle()).append("\n\n");
        }

        // Add main message
        formattedMessage.append(event.getMessage());

        // Add footer with app branding
        formattedMessage.append("\n\n");
        formattedMessage.append("---\n");
        formattedMessage.append("This is an automated notification from CareerMate.\n");
        formattedMessage.append("You can view all your notifications in the app.\n\n");
        formattedMessage.append("Best regards,\n");
        formattedMessage.append("The CareerMate Team\n");
        formattedMessage.append("\n");
        formattedMessage.append("© 2025 CareerMate. All rights reserved.");

        return formattedMessage.toString();
    }

    /**
     * Send real-time notification via Server-Sent Events (SSE).
     * Broadcasts the notification to all active SSE connections for the user.
     * Also sends updated unread count to keep notification bell badge current.
     *
     * @param notification The saved notification entity
     */
    private void sendSseNotification(Notification notification) {
        try {
            // Convert entity to response DTO
            NotificationResponse response = NotificationResponse.builder()
                    .id(notification.getId())
                    .eventId(notification.getEventId())
                    .eventType(notification.getEventType())
                    .recipientId(notification.getRecipientId())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .category(notification.getCategory())
                    .metadata(notification.getMetadata())
                    .priority(notification.getPriority())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .readAt(notification.getReadAt())
                    .build();

            // Send notification to user via SSE
            sseService.sendNotification(notification.getRecipientId(), response);

            // Calculate and send updated unread count
            long unreadCount = notificationRepo.countByRecipientIdAndIsReadFalse(notification.getRecipientId());
            sseService.sendUnreadCount(notification.getRecipientId(), (int) unreadCount);

            log.info("📡 Real-time notification sent via SSE | userId: {} | notificationId: {} | unreadCount: {}",
                    notification.getRecipientId(), notification.getId(), unreadCount);

        } catch (Exception e) {
            // Don't fail notification processing if SSE fails (user might not be connected)
            log.warn("⚠️ Failed to send SSE notification | userId: {} | notificationId: {} | error: {}",
                    notification.getRecipientId(), notification.getId(), e.getMessage());
        }
    }

    /**
     * Send push notification to mobile devices via Firebase Cloud Messaging.
     * Sends to all active device tokens registered for the user.
     * Failures (e.g., expired tokens) are handled gracefully without blocking
     * notification processing.
     *
     * @param notification The saved notification entity
     */
    private void sendPushNotification(Notification notification) {
        try {
            // Convert entity to response DTO
            NotificationResponse response = NotificationResponse.builder()
                    .id(notification.getId())
                    .eventId(notification.getEventId())
                    .eventType(notification.getEventType())
                    .recipientId(notification.getRecipientId())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .category(notification.getCategory())
                    .metadata(notification.getMetadata())
                    .priority(notification.getPriority())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .readAt(notification.getReadAt())
                    .build();

            // Send push notification to all user's devices
            int sentCount = fcmService.sendNotificationToUser(notification.getRecipientId(), response);

            if (sentCount > 0) {
                log.info("📱 Push notification sent | userId: {} | notificationId: {} | devices: {}",
                        notification.getRecipientId(), notification.getId(), sentCount);
            } else {
                log.debug("📱 No active devices found for push notification | userId: {}",
                        notification.getRecipientId());
            }

        } catch (Exception e) {
            // Don't fail notification processing if push fails (Firebase might be
            // unavailable)
            log.warn("⚠️ Failed to send push notification | userId: {} | notificationId: {} | error: {}",
                    notification.getRecipientId(), notification.getId(), e.getMessage());
        }
    }

    /**
     * Update heartbeat for health monitoring
     */
    private void updateHeartbeat(boolean success, String errorMessage) {
        try {
            NotificationHeartbeat heartbeat = heartbeatRepo.findByName("notification-worker")
                    .orElse(NotificationHeartbeat.builder()
                            .name("notification-worker")
                            .lastProcessedAt(Instant.now())
                            .messageCount(0L)
                            .errorCount(0L)
                            .build());

            heartbeat.setLastProcessedAt(Instant.now());
            heartbeat.setMessageCount(heartbeat.getMessageCount() + 1);

            if (!success) {
                heartbeat.setErrorCount(heartbeat.getErrorCount() + 1);
                heartbeat.setLastErrorMessage(errorMessage);
            }

            heartbeatRepo.save(heartbeat);
        } catch (Exception e) {
            log.error("Failed to update heartbeat", e);
        }
    }
}
