package com.astraval.coreflow.main_modules.ordersnapshot;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.main_modules.companies.Companies;
import com.astraval.coreflow.main_modules.customer.Customers;
import com.astraval.coreflow.main_modules.vendor.Vendors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_snapshot")
@EntityListeners(AuditingEntityListener.class)
public class OrderSnapshot {

  @Id
  @Column(name = "order_id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long orderId;

  @Column(name = "order_number")
  private String orderNumber; // ORD-YYYYMM-SEQ - to generate this number use "SELECT
                              // generate_order_number(:companyId);" function in database

  @Column(name = "order_reference")
  private Long orderReference; // Reference to the original OrderDetails ID

  @CreatedDate
  @Column(name = "order_date")
  private LocalDateTime orderDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer")
  private Customers customers = null;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vendor")
  private Vendors vendors = null;

  @Transient
  public Companies getSellerCompany() {
      return customers != null ? customers.getCompany() : null;
  }

  @Transient
  public Companies getBuyerCompany() {
      return vendors != null ? vendors.getCompany() : null;
  }

  @PositiveOrZero(message = "Tax amount must be positive or zero at Entity")
  @Column(name = "tax_amount")
  private Double taxAmount;

  @PositiveOrZero(message = "Discount amount must be positive or zero at Entity")
  @Column(name = "discount_amount")
  private Double discountAmount;

  @PositiveOrZero(message = "Delivery charge must be positive or zero at Entity")
  @Column(name = "delivery_charge")
  private Double deliveryCharge;

  @PositiveOrZero(message = "Order amount must be positive or zero at Entity")
  @Column(name = "order_amount")
  private Double orderAmount; // total excluding tax and discount

  @PositiveOrZero(message = "Total amount must be positive or zero at Entity")
  @Column(name = "total_amount")
  private Double totalAmount;

  @PositiveOrZero(message = "Paid amount must be positive or zero at Entity")
  @Column(name = "paid_amount")
  private Double paidAmount = 0.0;

  @Column(name = "order_status")
  private String orderStatus;

  @Column(name = "has_bill")
  private Boolean hasBill = false;

  @Column(name = "platform_ref")
  private String platformRef;

  // Default fields..
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
