package com.astraval.coreflow.modules.payments.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.astraval.coreflow.modules.payments.dto.PaymentOrderAllocationDto;
import com.astraval.coreflow.modules.payments.dto.PaymentViewDto;
import com.astraval.coreflow.modules.payments.model.Payments;
import com.astraval.coreflow.modules.payments.repo.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
  
    public PaymentViewDto getPaymentViewById(Long paymentId) {
        Payments payment = paymentRepository.findPaymentWithDetailsById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        
        List<PaymentOrderAllocationDto> allocations = paymentRepository
                .findPaymentOrderAllocationsByPaymentId(paymentId);
        
        return mapToPaymentViewDto(payment, allocations);
    }
    
    private PaymentViewDto mapToPaymentViewDto(Payments payment, List<PaymentOrderAllocationDto> allocations) {
        PaymentViewDto dto = new PaymentViewDto();
        
        // Payment details
        dto.setPaymentId(payment.getPaymentId());
        dto.setPaymentNumber(payment.getPaymentNumber());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAmount(payment.getAmount());
        dto.setModeOfPayment(payment.getModeOfPayment());
        dto.setReferenceNumber(payment.getReferenceNumber());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentRemarks(payment.getPaymentRemarks());
        dto.setIsActive(payment.getIsActive());
        
        // Company details
        if (payment.getSenderComp() != null) {
            dto.setBuyerCompanyId(payment.getSenderComp().getCompanyId());
            dto.setBuyerCompanyName(payment.getSenderComp().getCompanyName());
        }
        if (payment.getReceiverComp() != null) {
            dto.setSellerCompanyId(payment.getReceiverComp().getCompanyId());
            dto.setSellerCompanyName(payment.getReceiverComp().getCompanyName());
        }
        
        // Vendor details
        if (payment.getVendors() != null) {
            dto.setVendorId(payment.getVendors().getVendorId());
            dto.setVendorName(payment.getVendors().getVendorName());
            dto.setVendorDisplayName(payment.getVendors().getDisplayName());
            dto.setVendorEmail(payment.getVendors().getEmail());
            dto.setVendorPhone(payment.getVendors().getPhone());
        }
        
        // Customer details
        if (payment.getCustomers() != null) {
            dto.setCustomerId(payment.getCustomers().getCustomerId());
            dto.setCustomerName(payment.getCustomers().getCustomerName());
            dto.setCustomerDisplayName(payment.getCustomers().getDisplayName());
            dto.setCustomerEmail(payment.getCustomers().getEmail());
            dto.setCustomerPhone(payment.getCustomers().getPhone());
        }
        
        // Order allocations
        dto.setOrderAllocations(allocations);
        
        return dto;
    }
}
