package com.astraval.coreflow.modules.payments.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.modules.payments.dto.CreatePaymentOrderAllocation;
import com.astraval.coreflow.modules.payments.dto.CreateSellerPayment;
import com.astraval.coreflow.modules.payments.model.PaymentOrderAllocations;
import com.astraval.coreflow.modules.payments.model.Payments;
import com.astraval.coreflow.modules.payments.repo.PaymentOrderAllocationRepository;
import com.astraval.coreflow.modules.payments.repo.PaymentRepository;

@Service
public class SellerPaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentOrderAllocationRepository paymentOrderAllocationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;


    @Transactional
    public Long createSellerPayment(Long companyId, CreateSellerPayment request) {
        Companies sellerCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Customers customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Payments payment = new Payments();
        payment.setSellerCompany(sellerCompany);
        payment.setBuyerCompany(customer.getCustomerCompany());
        
        if (customer.getCustomerCompany() != null) {
            payment.setCustomers(customer);
            // Find vendor relationship if customer has a company
            // This would need vendor service method to find vendor by customer company
        }

        setPaymentDetails(payment, request.getPaymentDetails());
        payment.setPaymentStatus("RECEIVED");

        Payments savedPayment = paymentRepository.save(payment);
        
        // Create order allocations if provided
        if (request.getPaymentDetails().getOrderAllocations() != null) {
            createOrderAllocations(savedPayment, request.getPaymentDetails().getOrderAllocations());
        }

        return savedPayment.getPaymentId();
    }

    private void setPaymentDetails(Payments payment, com.astraval.coreflow.modules.payments.dto.CreatePayment paymentDetails) {
        payment.setAmount(paymentDetails.getAmount());
        payment.setPaymentDate(paymentDetails.getPaymentDate());
        payment.setModeOfPayment(paymentDetails.getModeOfPayment());
        payment.setReferenceNumber(paymentDetails.getReferenceNumber());
        payment.setPaymentRemarks(paymentDetails.getPaymentRemarks());

        // if (paymentDetails.getPaymentProofFileId() != null) {
        //     FileStorage file = fileStorageRepository.findById(paymentDetails.getPaymentProofFileId())
        //             .orElseThrow(() -> new RuntimeException("Payment proof file not found"));
        //     payment.setPaymentProofFile(file);
        // }
    }
    
    private void createOrderAllocations(Payments payment, java.util.List<CreatePaymentOrderAllocation> allocations) {
        allocations.forEach(allocationDto -> {
            OrderDetails order = orderDetailsRepository.findById(allocationDto.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + allocationDto.getOrderId()));
            
            PaymentOrderAllocations allocation = new PaymentOrderAllocations();
            allocation.setPayments(payment);
            allocation.setOrderDetails(order);
            allocation.setAmountApplied(allocationDto.getAmountApplied());
            allocation.setAllocationDate(allocationDto.getAllocationDate() != null ? 
                    allocationDto.getAllocationDate() : LocalDateTime.now());
            allocation.setAllocationRemarks(allocationDto.getAllocationRemarks());
            
            paymentOrderAllocationRepository.save(allocation);
        });
    }
}