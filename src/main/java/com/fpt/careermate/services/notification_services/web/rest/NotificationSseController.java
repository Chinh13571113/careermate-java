package com.fpt.careermate.services.notification_services.web.rest;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.notification_services.service.NotificationSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for Server-Sent Events (SSE) real-time notification streaming.
 * Provides endpoints for clients to establish long-lived HTTP connections
 * and receive real-time notifications as they occur.
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification SSE", description = "Real-time notification streaming via Server-Sent Events")
@RequiredArgsConstructor
@Slf4j
public class NotificationSseController {

    private final NotificationSseService sseService;

    /**
     * Establish an SSE connection for real-time notifications.
     * 
     * This endpoint creates a long-lived HTTP connection that streams notifications
     * to the client in real-time. The connection is authenticated via JWT bearer
     * token.
     * 
     * **How to use from frontend:**
     * ```javascript
     * const eventSource = new EventSource('/api/notifications/stream', {
     * headers: {
     * 'Authorization': 'Bearer YOUR_JWT_TOKEN'
     * }
     * });
     * 
     * // Listen for new notifications
     * eventSource.addEventListener('notification', (event) => {
     * const notification = JSON.parse(event.data);
     * console.log('New notification:', notification);
     * // Update UI with notification
     * });
     * 
     * // Listen for unread count updates
     * eventSource.addEventListener('unread-count', (event) => {
     * const { count } = JSON.parse(event.data);
     * console.log('Unread count:', count);
     * // Update notification bell badge
     * });
     * 
     * // Listen for connection established
     * eventSource.addEventListener('connected', (event) => {
     * console.log('Connected to notification stream');
     * });
     * 
     * // Handle errors
     * eventSource.onerror = (error) => {
     * console.error('SSE error:', error);
     * // EventSource will automatically try to reconnect
     * };
     * 
     * // Close connection when done
     * eventSource.close();
     * ```
     * 
     * **Events sent by server:**
     * - `connected`: Initial event confirming connection established
     * - `notification`: New notification received (NotificationResponse object)
     * - `unread-count`: Updated unread notification count ({ count: number })
     * - `keepalive`: Periodic ping to keep connection alive
     * 
     * **Connection management:**
     * - Timeout: 30 minutes of inactivity
     * - Auto-reconnect: Browser handles reconnection automatically
     * - Multiple tabs: Each tab gets its own connection, all receive same
     * notifications
     * - Authentication: JWT token must be valid and not expired
     * 
     * @return SseEmitter that streams notifications to the client
     * @throws AppException if user is not authenticated
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream real-time notifications", description = """
            Establish a Server-Sent Events (SSE) connection to receive real-time notifications.

            **Authentication**: Requires valid JWT token in Authorization header.

            **Events**:
            - `connected` - Connection established successfully
            - `notification` - New notification received (JSON object)
            - `unread-count` - Updated unread count (JSON: { count: number })
            - `keepalive` - Keep connection alive (sent periodically)

            **Usage**:
            ```javascript
            const eventSource = new EventSource('/api/notifications/stream', {
                headers: { 'Authorization': 'Bearer YOUR_TOKEN' }
            });

            eventSource.addEventListener('notification', (event) => {
                const notification = JSON.parse(event.data);
                // Handle new notification
            });
            ```

            **Connection Details**:
            - Timeout: 30 minutes
            - Auto-reconnect: Yes (handled by browser)
            - Multiple tabs: Supported (each gets own connection)

            **Mobile Support**:
            - React Native: Use EventSource polyfill (react-native-sse)
            - Flutter: Use eventsource package
            """)
    public SseEmitter streamNotifications() {
        String userId = getCurrentUserId();

        log.info("🔌 SSE connection request | userId: {}", userId);

        return sseService.createConnection(userId);
    }

    /**
     * Get the current authenticated user ID from SecurityContext.
     * 
     * @return User ID (email)
     * @throws AppException if user is not authenticated
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("❌ Unauthenticated SSE connection attempt");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
