package com.astraval.coreflow.modules.payments.service;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerService;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.modules.payments.PaymentStatus;
import com.astraval.coreflow.modules.payments.dto.CreateBuyerPayment;
import com.astraval.coreflow.modules.payments.dto.CreatePaymentOrderAllocation;
import com.astraval.coreflow.modules.payments.model.PaymentOrderAllocations;
import com.astraval.coreflow.modules.payments.model.Payments;
import com.astraval.coreflow.modules.payments.repo.PaymentOrderAllocationRepository;
import com.astraval.coreflow.modules.payments.repo.PaymentRepository;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssets;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssetsRepository;
import com.astraval.coreflow.modules.vendor.VendorRepository;
import com.astraval.coreflow.modules.vendor.Vendors;

@Service
public class BuyerPaymentService {
  
  
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentOrderAllocationRepository paymentOrderAllocationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private UserCompanyAssetsRepository userCompanyAssetsRepository;
    
    @Autowired
    private CustomerService customerService;
    
    
  @Transactional
    public Long createBuyerPayment(Long companyId, CreateBuyerPayment request) {
        Companies buyerCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // -------------------------------------
        // Get company assets from view
        UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
        if (companyAssets == null) {
            throw new RuntimeException("No assets found for company");
        }

        // Check vendor belongs to company
        if (companyAssets.getVendors() == null
                || !Arrays.asList(companyAssets.getVendors()).contains(request.getVendorId())) {
            throw new RuntimeException("Vendor does not belong to the requesting company");
        }

        Vendors vendor = vendorRepository.findById(request.getVendorId())
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        // -------------------------------------
        

        Payments payment = new Payments();
        payment.setBuyerCompany(buyerCompany);
        payment.setVendors(vendor);
        
        if (vendor.getVendorCompany() != null) {
            payment.setSellerCompany(vendor.getVendorCompany());
            // Find customer relationship if vendor has a company
            // This would need customer service method to find customer by vendor company
            Long vendorsCustomerCompanyId = vendor.getVendorCompany().getCompanyId();
            Customers sellerCustomer = customerService.getSellersCustomerId(vendorsCustomerCompanyId, companyId);
            payment.setCustomers(sellerCustomer);
        }

        // Create Payment Details
        setPaymentDetails(payment, request.getPaymentDetails());
        payment.setPaymentStatus(PaymentStatus.getPaid());

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
            
            // Check if order is already allocated to any payment
            boolean orderAlreadyAllocated = paymentOrderAllocationRepository
                    .existsByOrderDetailsOrderId(allocationDto.getOrderId());
            if (orderAlreadyAllocated) {
                throw new RuntimeException("Order " + allocationDto.getOrderId() + " is already allocated to another payment");
            }
            
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
