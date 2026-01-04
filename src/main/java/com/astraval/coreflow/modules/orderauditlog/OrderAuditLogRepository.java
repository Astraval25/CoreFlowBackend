package com.astraval.coreflow.modules.orderauditlog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAuditLogRepository extends JpaRepository<OrderAuditLog, Long> {
  
    List<OrderAuditLog> findByOrderIdOrderByChangedAtDesc(Long orderId);
    
    List<OrderAuditLog> findByOrderIdAndOrderItemIdOrderByChangedAtDesc(Long orderId, Long orderItemId);
}
