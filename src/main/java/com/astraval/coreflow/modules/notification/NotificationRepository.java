package com.astraval.coreflow.modules.notification;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByToCompanyCompanyIdOrderByCreatedDtDesc(Long toCompanyId);

    Page<Notification> findByToCompanyCompanyId(Long toCompanyId, Pageable pageable);

    List<Notification> findByToCompanyCompanyIdAndIsReadFalse(Long toCompanyId);

    Optional<Notification> findByNotificationIdAndToCompanyCompanyId(Long notificationId, Long toCompanyId);

    Long countByToCompanyCompanyIdAndIsReadFalse(Long toCompanyId);
}
