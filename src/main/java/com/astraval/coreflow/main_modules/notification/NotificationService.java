package com.astraval.coreflow.main_modules.notification;

import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.devicetoken.FirebaseMessagingService;
import com.astraval.coreflow.main_modules.notification.dto.CreateNotificationRequest;
import com.astraval.coreflow.main_modules.notification.dto.NotificationOpenResponse;
import com.astraval.coreflow.main_modules.notification.dto.NotificationPageDto;
import com.astraval.coreflow.main_modules.notification.dto.NotificationViewDto;
import com.astraval.coreflow.main_modules.user.User;
import com.astraval.coreflow.main_modules.user.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

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
        String normalizedActionUrl = normalizeActionUrl(request.getActionUrl());
        notification.setActionUrl(normalizedActionUrl);
        notification.setEntityKey(resolveEntityKey(request, normalizedActionUrl));
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);

        // Send FCM push notification
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(saved.getNotificationId()));
        data.put("title", request.getTitle());
        data.put("body", request.getMessage());
        data.put("type", request.getType());
        if (normalizedActionUrl != null) {
            data.put("actionUrl", normalizedActionUrl);
        }
        if (request.getToCompanyId() != null) {
            data.put("toCompanyId", String.valueOf(request.getToCompanyId()));
        }
        if (saved.getEntityKey() != null) {
            data.put("entityKey", saved.getEntityKey());
        }
        Long unreadCount = notificationRepository.countByToCompanyCompanyIdAndIsReadFalse(request.getToCompanyId());
        Long entityUnreadCount = getUnreadCountsByEntity(request.getToCompanyId())
                .getOrDefault(saved.getEntityKey(), 0L);
        data.put("unreadCount", String.valueOf(unreadCount));
        data.put("entityUnreadCount", String.valueOf(entityUnreadCount));
        data.put("badge", String.valueOf(unreadCount));
        firebaseMessagingService.sendToCompanyUsers(
                request.getToCompanyId(), request.getTitle(), request.getMessage(), data);

        return saved.getNotificationId();
    }

    @Transactional
    public Long createCompanyNotification(Long fromCompanyId, Long toCompanyId, String title, String message,
            String type, String actionLabel, String actionUrl) {
        return createCompanyNotification(fromCompanyId, toCompanyId, title, message, type, actionLabel, actionUrl, null);
    }

    @Transactional
    public Long createCompanyNotification(Long fromCompanyId, Long toCompanyId, String title, String message,
            String type, String actionLabel, String actionUrl, String entityKey) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setFromCompanyId(fromCompanyId);
        request.setToCompanyId(toCompanyId);
        request.setTitle(title);
        request.setMessage(message);
        request.setType(type);
        request.setActionLabel(actionLabel);
        request.setActionUrl(actionUrl);
        request.setEntityKey(entityKey);
        return createNotification(request);
    }

    public NotificationPageDto getCompanyNotifications(Long companyId, int page) {
        int pageSize = 6;
        int safePage = Math.max(page, 0);
        PageRequest pageRequest = PageRequest.of(safePage, pageSize, Sort.by(Sort.Direction.DESC, "createdDt"));
        Page<Notification> notificationPage = notificationRepository.findByToCompanyCompanyIdAndIsReadFalse(companyId, pageRequest);
        Map<String, Long> unreadCountByEntity = getUnreadCountsByEntity(companyId);
        long totalUnreadCount = notificationRepository.countByToCompanyCompanyIdAndIsReadFalse(companyId);

        NotificationPageDto response = new NotificationPageDto();
        response.setNotifications(notificationPage.getContent().stream()
                .map(notification -> toViewDto(notification, unreadCountByEntity))
                .toList());
        response.setPage(notificationPage.getNumber());
        response.setSize(notificationPage.getSize());
        response.setTotalElements(notificationPage.getTotalElements());
        response.setTotalPages(notificationPage.getTotalPages());
        response.setHasNext(notificationPage.hasNext());
        response.setHasPrevious(notificationPage.hasPrevious());
        response.setTotalUnreadCount(totalUnreadCount);
        response.setUnreadCountByEntity(unreadCountByEntity);
        return response;
    }

    public Long getCompanyUnreadCount(Long companyId) {
        return notificationRepository.countByToCompanyCompanyIdAndIsReadFalse(companyId);
    }

    public Map<String, Long> getCompanyUnreadCountByEntity(Long companyId) {
        return getUnreadCountsByEntity(companyId);
    }

    @Transactional
    public void markAsRead(Long companyId, Long notificationId) {
        Long deleted = notificationRepository.deleteByNotificationIdAndToCompanyCompanyId(notificationId, companyId);
        if (deleted == null || deleted == 0) {
            throw new RuntimeException("Notification not found with ID: " + notificationId);
        }
    }

    @Transactional
    public Long markAllAsRead(Long companyId) {
        Long deleted = notificationRepository.deleteByToCompanyCompanyIdAndIsReadFalse(companyId);
        return deleted == null ? 0L : deleted;
    }

    @Transactional
    public NotificationOpenResponse openNotification(Long companyId, Long notificationId) {
        Notification notification = notificationRepository.findByNotificationIdAndToCompanyCompanyId(notificationId, companyId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        notificationRepository.delete(notification);

        return new NotificationOpenResponse(
                notification.getNotificationId(),
                notification.getActionLabel(),
                notification.getActionUrl());
    }

    private NotificationViewDto toViewDto(Notification notification, Map<String, Long> unreadCountByEntity) {
        NotificationViewDto dto = new NotificationViewDto();
        dto.setNotificationId(notification.getNotificationId());
        dto.setFromCompanyId(notification.getFromCompany() != null ? notification.getFromCompany().getCompanyId() : null);
        dto.setToCompanyId(notification.getToCompany() != null ? notification.getToCompany().getCompanyId() : null);
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setActionLabel(notification.getActionLabel());
        dto.setActionUrl(notification.getActionUrl());
        dto.setEntityKey(notification.getEntityKey());
        dto.setEntityUnreadCount(unreadCountByEntity.getOrDefault(notification.getEntityKey(), 0L));
        dto.setIsRead(notification.getIsRead());
        dto.setReadDt(notification.getReadDt());
        dto.setCreatedDt(notification.getCreatedDt());
        return dto;
    }

    private Map<String, Long> getUnreadCountsByEntity(Long companyId) {
        Map<String, Long> counts = new LinkedHashMap<>();
        notificationRepository.countUnreadByEntity(companyId).forEach(group -> {
            String key = normalizeEntityKey(group.getEntityKey());
            if (key != null) {
                counts.put(key, group.getUnreadCount());
            }
        });
        return counts;
    }

    private String normalizeActionUrl(String actionUrl) {
        if (actionUrl == null || actionUrl.isBlank()) {
            return null;
        }

        String normalized = actionUrl.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        if (normalized.startsWith("/cf/")) {
            return normalized;
        }

        if (normalized.startsWith("/companies/") || normalized.startsWith("/company/")) {
            return "/cf" + normalized;
        }

        return normalized;
    }

    private String resolveEntityKey(CreateNotificationRequest request, String actionUrl) {
        if (request.getEntityKey() != null && !request.getEntityKey().isBlank()) {
            return normalizeEntityKey(request.getEntityKey());
        }

        String fromPath = resolveEntityKeyFromPath(actionUrl);
        if (fromPath != null) {
            return fromPath;
        }

        if (request.getType() != null && !request.getType().isBlank()) {
            return normalizeEntityKey(request.getType());
        }

        return "GENERAL";
    }

    private String resolveEntityKeyFromPath(String actionUrl) {
        if (actionUrl == null || actionUrl.isBlank()) {
            return null;
        }

        String lowerPath = actionUrl.toLowerCase();
        if (lowerPath.contains("/purchase/")) return "PURCHASE";
        if (lowerPath.contains("/sales/")) return "SALES";
        if (lowerPath.contains("/payments/received") || lowerPath.contains("/payment-received")) return "PAYMENT_RECEIVED";
        if (lowerPath.contains("/payments/sent") || lowerPath.contains("/payment-made")) return "PAYMENT_SENT";
        if (lowerPath.contains("/expenses")) return "EXPENSE";
        if (lowerPath.contains("/customers")) return "CUSTOMER";
        if (lowerPath.contains("/vendors")) return "VENDOR";
        if (lowerPath.contains("/work-logs")) return "WORK_LOG";
        if (lowerPath.contains("/leave-logs") || lowerPath.contains("/employee-leave-requests")) return "LEAVE_REQUEST";

        return null;
    }

    private String normalizeEntityKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase();
    }

}
