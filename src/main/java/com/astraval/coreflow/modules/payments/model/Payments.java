package com.astraval.coreflow.modules.payments.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.filestorage.FileStorage;
import com.astraval.coreflow.modules.vendor.Vendors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
public class Payments {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")
  private Long paymentId;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_company")
  private Companies sellerCompany = null;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_company")
  private Companies buyerCompany = null;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer")
  private Customers customers = null;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vendor")
  private Vendors vendors = null;
  
  @Column(name = "payment_number")
  private Long paymentNumber;     // To-do
  
  @Column(name = "payment_date")
  private LocalDateTime paymentDate;
  
  @Column(name = "amount")
  private Double amount;
  
  @Column(name = "mode_of_payment")
  private String modeOfPayment;
  
  @Column(name = "reference_number") // cheque no, UTR, etc.
  private String referenceNumber;
  
  @Column(name = "payment_status")
  private String paymentStatus;
  
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_proof_file")
  private FileStorage paymentProofFile;  // To-do

  @Column(name = "payment_remarks")
  private String paymentRemarks;

  
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
