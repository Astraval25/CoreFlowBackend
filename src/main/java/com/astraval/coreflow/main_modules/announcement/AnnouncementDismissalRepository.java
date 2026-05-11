package com.astraval.coreflow.main_modules.announcement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementDismissalRepository extends JpaRepository<AnnouncementDismissal, Long> {

    boolean existsByAnnouncementAnnouncementIdAndUserUserId(Long announcementId, Long userId);
}
