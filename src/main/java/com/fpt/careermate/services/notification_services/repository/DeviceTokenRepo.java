package com.fpt.careermate.services.notification_services.repository;

import com.fpt.careermate.services.notification_services.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DeviceToken entity operations
 */
@Repository
public interface DeviceTokenRepo extends JpaRepository<DeviceToken, Long> {

    /**
     * Find all active device tokens for a specific user
     * 
     * @param userId The user ID (email)
     * @return List of active device tokens
     */
    List<DeviceToken> findByUserIdAndIsActiveTrue(String userId);

    /**
     * Find a device token by its FCM token string
     * 
     * @param token The FCM token
     * @return Optional DeviceToken
     */
    Optional<DeviceToken> findByToken(String token);

    /**
     * Find all device tokens for a user (active and inactive)
     * 
     * @param userId The user ID (email)
     * @return List of all device tokens
     */
    List<DeviceToken> findByUserId(String userId);

    /**
     * Count active device tokens for a user
     * 
     * @param userId The user ID (email)
     * @return Number of active tokens
     */
    long countByUserIdAndIsActiveTrue(String userId);

    /**
     * Delete all inactive tokens for a user
     * 
     * @param userId The user ID (email)
     */
    void deleteByUserIdAndIsActiveFalse(String userId);
}
