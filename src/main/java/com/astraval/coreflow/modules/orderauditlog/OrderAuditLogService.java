package com.astraval.coreflow.modules.orderauditlog;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.modules.orderauditlog.dto.OrderAuditLogDto;

@Service
public class OrderAuditLogService {
  
    @Autowired
    private OrderAuditLogRepository orderAuditLogRepository;
    
    @Autowired
    private OrderAuditLogMapper orderAuditLogMapper;
    
    public void logOrderCreation(Long orderId) {
        OrderAuditLog auditLog = new OrderAuditLog();
        auditLog.setOrderId(orderId);
        auditLog.setEntityType("ORDER");
        auditLog.setFieldName("status");
        auditLog.setNewValue("PENDING");
        auditLog.setChangeType("CREATE");
        auditLog.setChangedByRole("BUYER");
        
        orderAuditLogRepository.save(auditLog);
    }
    
    public void logOrderStatusChange(Long orderId, String oldStatus, String newStatus) {
        OrderAuditLog auditLog = new OrderAuditLog();
        auditLog.setOrderId(orderId);
        auditLog.setEntityType("ORDER");
        auditLog.setFieldName("status");
        auditLog.setOldValue(oldStatus);
        auditLog.setNewValue(newStatus);
        auditLog.setChangeType("UPDATE");
        auditLog.setChangedByRole("SELLER");
        
        orderAuditLogRepository.save(auditLog);
    }
    
    public void logOrderItemCreation(Long orderId, Long orderItemId) {
        OrderAuditLog auditLog = new OrderAuditLog();
        auditLog.setOrderId(orderId);
        auditLog.setOrderItemId(orderItemId);
        auditLog.setEntityType("ORDER_ITEM");
        auditLog.setFieldName("status");
        auditLog.setNewValue("PENDING");
        auditLog.setChangeType("CREATE");
        auditLog.setChangedByRole("BUYER");
        
        orderAuditLogRepository.save(auditLog);
    }
    
    public void logOrderItemQuantityChange(Long orderId, Long orderItemId, String oldQuantity, String newQuantity) {
        OrderAuditLog auditLog = new OrderAuditLog();
        auditLog.setOrderId(orderId);
        auditLog.setOrderItemId(orderItemId);
        auditLog.setEntityType("ORDER_ITEM");
        auditLog.setFieldName("quantity");
        auditLog.setOldValue(oldQuantity);
        auditLog.setNewValue(newQuantity);
        auditLog.setChangeType("UPDATE");
        auditLog.setChangedByRole("BUYER");
        
        orderAuditLogRepository.save(auditLog);
    }
    
    public void logOrderItemPriceChange(Long orderId, Long orderItemId, String oldPrice, String newPrice) {
        OrderAuditLog auditLog = new OrderAuditLog();
        auditLog.setOrderId(orderId);
        auditLog.setOrderItemId(orderItemId);
        auditLog.setEntityType("ORDER_ITEM");
        auditLog.setFieldName("price");
        auditLog.setOldValue(oldPrice);
        auditLog.setNewValue(newPrice);
        auditLog.setChangeType("UPDATE");
        auditLog.setChangedByRole("SELLER");
        
        orderAuditLogRepository.save(auditLog);
    }
    
    public List<OrderAuditLogDto> getOrderAuditLogs(Long orderId) {
        List<OrderAuditLog> auditLogs = orderAuditLogRepository.findByOrderIdOrderByChangedAtDesc(orderId);
        return auditLogs.stream()
                .map(orderAuditLogMapper::toOrderAuditLogDto)
                .toList();
    }
}
