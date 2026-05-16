package com.astraval.coreflow.main_modules.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByToCompanyCompanyIdOrderByCreatedDtDesc(Long toCompanyId);

    Page<Notification> findByToCompanyCompanyId(Long toCompanyId, Pageable pageable);

    Page<Notification> findByToCompanyCompanyIdAndIsReadFalse(Long toCompanyId, Pageable pageable);

    List<Notification> findByToCompanyCompanyIdAndIsReadFalse(Long toCompanyId);

    Optional<Notification> findByNotificationIdAndToCompanyCompanyId(Long notificationId, Long toCompanyId);

    Long countByToCompanyCompanyIdAndIsReadFalse(Long toCompanyId);

    Long deleteByNotificationIdAndToCompanyCompanyId(Long notificationId, Long toCompanyId);

    Long deleteByToCompanyCompanyIdAndSubjectTypeAndSubjectIdAndIsReadFalse(
            Long toCompanyId,
            String subjectType,
            Long subjectId);

    Long deleteByToCompanyCompanyIdAndIsReadFalse(Long toCompanyId);

    @Query("""
            SELECT COALESCE(n.entityKey, n.type) AS entityKey, COUNT(n) AS unreadCount
            FROM Notification n
            WHERE n.toCompany.companyId = :companyId AND n.isRead = false
            GROUP BY COALESCE(n.entityKey, n.type)
            ORDER BY COUNT(n) DESC
            """)
    List<NotificationEntityUnreadCount> countUnreadByEntity(@Param("companyId") Long companyId);

    @Query("""
            SELECT n.subjectId AS subjectId, COUNT(n) AS unreadCount
            FROM Notification n
            WHERE n.toCompany.companyId = :companyId
              AND n.isRead = false
              AND n.subjectType = :subjectType
              AND n.subjectId IS NOT NULL
            GROUP BY n.subjectId
            """)
    List<NotificationSubjectUnreadCount> countUnreadBySubjectType(
            @Param("companyId") Long companyId,
            @Param("subjectType") String subjectType);
}
