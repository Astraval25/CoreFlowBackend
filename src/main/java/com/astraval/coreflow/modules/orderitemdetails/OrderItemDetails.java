package com.astraval.coreflow.modules.orderitemdetails;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_item_details")
@EntityListeners(AuditingEntityListener.class)
public class OrderItemDetails {

  @Id
  @Column(name = "order_item_id")
  private Long orderItemId;
  
  @Column(name = "order_id")
  private Long orderId;
  
  @Column(name = "item_id")
  private Long itemId;
  
  @Column(name = "quantity")
  private Integer quantity;
  
  @Column(name = "base_price")
  private Double basePrice;
  
  @Column(name = "updated_price")
  private Double updatedPrice;
  
  @Column(name = "unit_of_measure")
  private String unitOfMeasure;
  
  @Column(name= "status")
  private String status;
  
  
  // Default fields...
  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @CreatedBy
  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @CreatedDate
  @Column(name = "created_dt", nullable = false)
  private LocalDateTime createdDt;

  @LastModifiedBy
  @Column(name = "modified_by")
  private Long lastModifiedBy;

  @LastModifiedDate
  @Column(name = "modified_dt")
  private LocalDateTime lastModifiedDt;
}

