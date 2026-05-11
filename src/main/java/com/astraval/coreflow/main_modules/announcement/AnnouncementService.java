package com.astraval.coreflow.main_modules.announcement;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.announcement.dto.AnnouncementViewDto;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementDismissalRepository dismissalRepository;
    private final UserRepository userRepository;

    public AnnouncementService(
            AnnouncementRepository announcementRepository,
            AnnouncementDismissalRepository dismissalRepository,
            UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.dismissalRepository = dismissalRepository;
        this.userRepository = userRepository;
    }

    public Optional<AnnouncementViewDto> getCurrentAnnouncement() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            return Optional.empty();
        }

        Long userId = currentUserId(auth);
        Optional<Announcement> latestActive = announcementRepository
                .findByIsActiveTrueOrderByCreatedDtDescAnnouncementIdDesc(PageRequest.of(0, 1))
                .stream()
                .findFirst();

        if (latestActive.isEmpty()) {
            return Optional.empty();
        }

        Announcement announcement = latestActive.get();
        if (dismissalRepository.existsByAnnouncementAnnouncementIdAndUserUserId(
                announcement.getAnnouncementId(), userId)) {
            return Optional.empty();
        }

        return Optional.of(toViewDto(announcement));
    }

    @Transactional
    public void dismissAnnouncement(Long announcementId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(auth)) {
            return;
        }

        Long userId = currentUserId(auth);
        if (dismissalRepository.existsByAnnouncementAnnouncementIdAndUserUserId(announcementId, userId)) {
            return;
        }

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("Announcement not found with ID: " + announcementId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        AnnouncementDismissal dismissal = new AnnouncementDismissal();
        dismissal.setAnnouncement(announcement);
        dismissal.setUser(user);
        dismissalRepository.save(dismissal);
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADM".equals(authority.getAuthority()));
    }

    private Long currentUserId(Authentication auth) {
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid authenticated user");
        }
    }

    private AnnouncementViewDto toViewDto(Announcement announcement) {
        AnnouncementViewDto dto = new AnnouncementViewDto();
        dto.setAnnouncementId(announcement.getAnnouncementId());
        dto.setAnnouncementKey(announcement.getAnnouncementKey());
        dto.setTitle(announcement.getTitle());
        dto.setMessage(announcement.getMessage());
        dto.setActionLabel(announcement.getActionLabel());
        dto.setActionUrl(announcement.getActionUrl());
        dto.setCreatedDt(announcement.getCreatedDt());
        return dto;
    }
}
