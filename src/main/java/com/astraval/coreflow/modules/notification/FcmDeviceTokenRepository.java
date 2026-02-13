package com.astraval.coreflow.modules.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmDeviceTokenRepository extends JpaRepository<FcmDeviceToken, Long> {

    Optional<FcmDeviceToken> findByFcmToken(String fcmToken);

    @Query("""
            SELECT t.fcmToken
            FROM FcmDeviceToken t
            WHERE t.user.userId IN :userIds
              AND t.isActive = true
            """)
    List<String> findActiveTokensByUserIds(@Param("userIds") List<Long> userIds);

    Optional<FcmDeviceToken> findByUserUserIdAndFcmTokenAndIsActiveTrue(Long userId, String fcmToken);
}
