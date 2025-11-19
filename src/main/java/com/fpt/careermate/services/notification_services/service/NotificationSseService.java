package com.fpt.careermate.services.notification_services.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service to manage Server-Sent Events (SSE) connections for real-time
 * notifications.
 * Maintains active connections per user and broadcasts notifications to
 * connected clients.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationSseService {

    private final ObjectMapper objectMapper;

    // Store multiple SSE connections per user (user can have multiple tabs/devices)
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> userConnections = new ConcurrentHashMap<>();

    // SSE timeout: 30 minutes (1800000ms)
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    /**
     * Create a new SSE connection for a user.
     *
     * @param userId The user ID
     * @return SseEmitter for the connection
     */
    public SseEmitter createConnection(String userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Add connection to user's connection list
        userConnections.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        log.info("📡 SSE connection established | userId: {} | totalConnections: {}",
                userId, userConnections.get(userId).size());

        // Handle completion (client closes connection normally)
        emitter.onCompletion(() -> {
            removeConnection(userId, emitter);
            log.info("✅ SSE connection completed | userId: {}", userId);
        });

        // Handle timeout (connection idle for too long)
        emitter.onTimeout(() -> {
            removeConnection(userId, emitter);
            log.warn("⏱️ SSE connection timeout | userId: {}", userId);
        });

        // Handle errors (network issues, client disconnect)
        emitter.onError((error) -> {
            removeConnection(userId, emitter);
            log.error("❌ SSE connection error | userId: {} | error: {}", userId, error.getMessage());
        });

        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "message", "Connected to notification stream",
                            "userId", userId,
                            "timestamp", System.currentTimeMillis())));
        } catch (IOException e) {
            log.error("❌ Failed to send initial SSE event | userId: {}", userId, e);
            removeConnection(userId, emitter);
        }

        return emitter;
    }

    /**
     * Send a notification to a specific user via SSE.
     * If user has multiple connections (tabs/devices), send to all.
     *
     * @param userId       The recipient user ID
     * @param notification The notification to send
     */
    public void sendNotification(String userId, NotificationResponse notification) {
        CopyOnWriteArrayList<SseEmitter> connections = userConnections.get(userId);

        if (connections == null || connections.isEmpty()) {
            log.debug("⚠️ No active SSE connections for user: {}", userId);
            return;
        }

        log.info("📨 Broadcasting SSE notification | userId: {} | connections: {} | eventType: {}",
                userId, connections.size(), notification.getEventType());

        // Send to all user's connections
        connections.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(notification));

                log.debug("✅ SSE notification sent | userId: {} | notificationId: {}",
                        userId, notification.getId());

            } catch (IOException e) {
                log.error("❌ Failed to send SSE notification | userId: {} | notificationId: {} | error: {}",
                        userId, notification.getId(), e.getMessage());
                removeConnection(userId, emitter);
            }
        });
    }

    /**
     * Send unread count update to a specific user.
     *
     * @param userId      The user ID
     * @param unreadCount The number of unread notifications
     */
    public void sendUnreadCount(String userId, int unreadCount) {
        CopyOnWriteArrayList<SseEmitter> connections = userConnections.get(userId);

        if (connections == null || connections.isEmpty()) {
            return;
        }

        log.info("🔔 Broadcasting unread count | userId: {} | count: {}", userId, unreadCount);

        connections.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("unread-count")
                        .data(Map.of("count", unreadCount)));
            } catch (IOException e) {
                log.error("❌ Failed to send unread count | userId: {}", userId);
                removeConnection(userId, emitter);
            }
        });
    }

    /**
     * Send a keepalive ping to maintain connection.
     * Called periodically to prevent connection timeout.
     *
     * @param userId The user ID
     */
    public void sendKeepalive(String userId) {
        CopyOnWriteArrayList<SseEmitter> connections = userConnections.get(userId);

        if (connections == null || connections.isEmpty()) {
            return;
        }

        connections.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("keepalive")
                        .data("ping"));
            } catch (IOException e) {
                removeConnection(userId, emitter);
            }
        });
    }

    /**
     * Remove a specific SSE connection for a user.
     *
     * @param userId  The user ID
     * @param emitter The emitter to remove
     */
    private void removeConnection(String userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> connections = userConnections.get(userId);
        if (connections != null) {
            connections.remove(emitter);
            if (connections.isEmpty()) {
                userConnections.remove(userId);
                log.info("🔌 All SSE connections closed for user: {}", userId);
            } else {
                log.info("🔌 SSE connection removed | userId: {} | remaining: {}", userId, connections.size());
            }
        }
        emitter.complete();
    }

    /**
     * Remove all connections for a user (e.g., on logout).
     *
     * @param userId The user ID
     */
    public void removeAllConnections(String userId) {
        CopyOnWriteArrayList<SseEmitter> connections = userConnections.remove(userId);
        if (connections != null) {
            connections.forEach(SseEmitter::complete);
            log.info("🔌 All SSE connections removed for user: {} | count: {}", userId, connections.size());
        }
    }

    /**
     * Get the number of active connections for a user.
     *
     * @param userId The user ID
     * @return Number of active connections
     */
    public int getConnectionCount(String userId) {
        CopyOnWriteArrayList<SseEmitter> connections = userConnections.get(userId);
        return connections != null ? connections.size() : 0;
    }

    /**
     * Get total number of active SSE connections across all users.
     *
     * @return Total connection count
     */
    public int getTotalConnectionCount() {
        return userConnections.values().stream()
                .mapToInt(CopyOnWriteArrayList::size)
                .sum();
    }

    /**
     * Get number of users with active connections.
     *
     * @return Number of connected users
     */
    public int getConnectedUserCount() {
        return userConnections.size();
    }
}
