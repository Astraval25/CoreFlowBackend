package com.astraval.coreflow.modules.payments.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentViewDto {
    // Payment details
    private Long paymentId;
    private Long paymentNumber;
    private LocalDateTime paymentDate;
    private Double amount;
    private String modeOfPayment;
    private String referenceNumber;
    private String paymentStatus;
    private String paymentRemarks;
    private Boolean isActive;
    
    // Company details
    private Long buyerCompanyId;
    private String buyerCompanyName;
    private Long sellerCompanyId;
    private String sellerCompanyName;
    
    // Vendor details
    private Long vendorId;
    private String vendorName;
    private String vendorDisplayName;
    private String vendorEmail;
    private String vendorPhone;
    
    // Customer details
    private Long customerId;
    private String customerName;
    private String customerDisplayName;
    private String customerEmail;
    private String customerPhone;
    
    // Order allocations
    private List<PaymentOrderAllocationDto> orderAllocations;
}