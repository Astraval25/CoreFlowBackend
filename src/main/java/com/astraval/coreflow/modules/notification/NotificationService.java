package com.astraval.coreflow.modules.notification;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.notification.dto.CreateNotificationRequest;
import com.astraval.coreflow.modules.notification.dto.NotificationOpenResponse;
import com.astraval.coreflow.modules.notification.dto.NotificationPageDto;
import com.astraval.coreflow.modules.notification.dto.NotificationViewDto;
import com.astraval.coreflow.modules.user.User;
import com.astraval.coreflow.modules.user.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public Long createNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
            notification.setUser(user);
        }

        Companies toCompany = companyRepository.findById(request.getToCompanyId())
                .orElseThrow(() -> new RuntimeException("To company not found with ID: " + request.getToCompanyId()));
        notification.setToCompany(toCompany);

        if (request.getFromCompanyId() != null) {
            Companies fromCompany = companyRepository.findById(request.getFromCompanyId())
                    .orElseThrow(() -> new RuntimeException("From company not found with ID: " + request.getFromCompanyId()));
            notification.setFromCompany(fromCompany);
        }

        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setActionLabel(request.getActionLabel());
        notification.setActionUrl(request.getActionUrl());
        notification.setIsRead(false);

        return notificationRepository.save(notification).getNotificationId();
    }

    @Transactional
    public Long createCompanyNotification(Long fromCompanyId, Long toCompanyId, String title, String message,
            String type, String actionLabel, String actionUrl) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setFromCompanyId(fromCompanyId);
        request.setToCompanyId(toCompanyId);
        request.setTitle(title);
        request.setMessage(message);
        request.setType(type);
        request.setActionLabel(actionLabel);
        request.setActionUrl(actionUrl);
        return createNotification(request);
    }

    public NotificationPageDto getCompanyNotifications(Long companyId, int page) {
        int pageSize = 6;
        int safePage = Math.max(page, 0);
        PageRequest pageRequest = PageRequest.of(safePage, pageSize, Sort.by(Sort.Direction.DESC, "createdDt"));
        Page<Notification> notificationPage = notificationRepository.findByToCompanyCompanyId(companyId, pageRequest);

        NotificationPageDto response = new NotificationPageDto();
        response.setNotifications(notificationPage.getContent().stream().map(this::toViewDto).toList());
        response.setPage(notificationPage.getNumber());
        response.setSize(notificationPage.getSize());
        response.setTotalElements(notificationPage.getTotalElements());
        response.setTotalPages(notificationPage.getTotalPages());
        response.setHasNext(notificationPage.hasNext());
        response.setHasPrevious(notificationPage.hasPrevious());
        return response;
    }

    public Long getCompanyUnreadCount(Long companyId) {
        return notificationRepository.countByToCompanyCompanyIdAndIsReadFalse(companyId);
    }

    @Transactional
    public void markAsRead(Long companyId, Long notificationId) {
        Notification notification = notificationRepository.findByNotificationIdAndToCompanyCompanyId(notificationId, companyId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadDt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public Long markAllAsRead(Long companyId) {
        List<Notification> notifications = notificationRepository.findByToCompanyCompanyIdAndIsReadFalse(companyId);
        LocalDateTime readTime = LocalDateTime.now();

        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadDt(readTime);
        });

        notificationRepository.saveAll(notifications);
        return (long) notifications.size();
    }

    @Transactional
    public NotificationOpenResponse openNotification(Long companyId, Long notificationId) {
        Notification notification = notificationRepository.findByNotificationIdAndToCompanyCompanyId(notificationId, companyId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadDt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return new NotificationOpenResponse(
                notification.getNotificationId(),
                notification.getActionLabel(),
                notification.getActionUrl());
    }

    private NotificationViewDto toViewDto(Notification notification) {
        NotificationViewDto dto = new NotificationViewDto();
        dto.setNotificationId(notification.getNotificationId());
        dto.setFromCompanyId(notification.getFromCompany() != null ? notification.getFromCompany().getCompanyId() : null);
        dto.setToCompanyId(notification.getToCompany() != null ? notification.getToCompany().getCompanyId() : null);
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setActionLabel(notification.getActionLabel());
        dto.setActionUrl(notification.getActionUrl());
        dto.setIsRead(notification.getIsRead());
        dto.setReadDt(notification.getReadDt());
        dto.setCreatedDt(notification.getCreatedDt());
        return dto;
    }

}
