package com.astraval.coreflow.modules.orderauditlog.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderAuditLogDto {
  private Long orderAuditId;
  private Long orderId;
  private Long orderItemId;
  private String entityType;
  private String fieldName;
  private String oldValue;
  private String newValue;
  private String changeType;
  private String reason;
  private String changedByRole;
  private Long changedBy;
  private LocalDateTime changedAt;
}