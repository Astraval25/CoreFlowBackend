package com.astraval.coreflow.modules.orderdetails;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.modules.companies.Companies;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "order_details")
@EntityListeners(AuditingEntityListener.class)
public class OrderDetails {

  @Id
  @Column(name = "order_id")
  private Long orderId;
  
  @Column(name = "order_number")
  private String orderNumber;  // ORD-YYYYMM-SEQ

  @Column(name = "order_date")
  private LocalDateTime orderDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_company")
  private Companies sellerCompany;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_company")
  private Companies buyerCompany;
  
  @Column(name = "order_amount")
  private Double orderAmount;
  
  @Column(name = "tax_amount")
  private Double taxAmount;
  
  @Column(name = "discount_amount")
  private Double discountAmount;
  
  @Column(name = "delivery_charge")
  private Double deliveryCharge;
  
  @Column(name = "order_status")
  private String orderStatus;
  

  
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
