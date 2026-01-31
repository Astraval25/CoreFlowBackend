package com.astraval.coreflow.modules.payments.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "payment_order_allocations")
@EntityListeners(AuditingEntityListener.class)
public class PaymentOrderAllocations {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_order_allocation_id")
  private Long paymentOrderAllocationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", foreignKey = @ForeignKey(name = "fk_payment_allocation", 
             foreignKeyDefinition = "FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE"))
  private Payments payments;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  private OrderDetails orderDetails;

  @Column(name = "amount_applied")
  private Double amountApplied;
  
  @Column(name = "allocation_date")
  private LocalDateTime allocationDate;

  @Column(name = "allocation_remarks")
  private String allocationRemarks;

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
