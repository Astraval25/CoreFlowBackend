package com.astraval.coreflow.modules.orderauditlog;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_audit_log")
@EntityListeners(AuditingEntityListener.class)
public class OrderAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long orderAuditId;

  // Scope
  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "order_item_id")
  private Long orderItemId; // NULL = order-level change

  // What changed
  @Column(name = "entity_type", nullable = false)
  private String entityType; // ORDER | ORDER_ITEM

  @Column(name = "field_name", nullable = false)
  private String fieldName; // status, quantity, price, tax, discount

  @Column(name = "old_value")
  private String oldValue;

  @Column(name = "new_value")
  private String newValue;

  // Change reason
  @Column(name = "change_type", nullable = false)
  private String changeType; // CREATE | UPDATE | CANCEL | SYSTEM_ADJUST

  @Column(name = "reason")
  private String reason;

  // Actor
  @Column(name = "changed_by_role")
  private String changedByRole; // BUYER | SELLER | ADMIN | SYSTEM
  
   @CreatedBy
  @Column(name = "changed_by", updatable = false)
  private Long changedBy;

  @CreatedDate
  @Column(name = "changed_at", updatable = false, nullable = false)
  private LocalDateTime changedAt;
}
