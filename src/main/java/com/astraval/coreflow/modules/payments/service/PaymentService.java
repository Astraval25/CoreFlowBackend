package com.astraval.coreflow.modules.payments.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.payments.PaymentStatus;
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

    @Transactional
    public void updatePaymentStatus(Long companyId, Long paymentId, String newStatus) {
        Payments payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        boolean belongsToCompany = (payment.getSenderComp() != null
                && payment.getSenderComp().getCompanyId().equals(companyId))
                || (payment.getReceiverComp() != null
                        && payment.getReceiverComp().getCompanyId().equals(companyId));

        if (!belongsToCompany) {
            throw new RuntimeException("Payment does not belong to the requesting company");
        }

        if (!PaymentStatus.isSupportedStatus(newStatus)) {
            throw new RuntimeException("Invalid payment status: " + newStatus);
        }

        if (PaymentStatus.getPaymentFailed().equals(newStatus)) {
            LocalDateTime paymentMadeAt = payment.getPaymentDate() != null ? payment.getPaymentDate() : payment.getCreatedDt();
            if (paymentMadeAt == null) {
                throw new RuntimeException("Payment made date is not available for status validation");
            }

            if (LocalDateTime.now().isAfter(paymentMadeAt.plusDays(5))) {
                throw new RuntimeException("PAYMENT_FAILED can only be updated within 5 days of payment made");
            }
        }

        payment.setPaymentStatus(newStatus);
        paymentRepository.save(payment);
    }
}
