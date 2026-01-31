package com.astraval.coreflow.modules.payments.service;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.OrderStatus;
import com.astraval.coreflow.modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.modules.payments.PaymentStatus;
import com.astraval.coreflow.modules.payments.dto.CreatePaymentDto;
import com.astraval.coreflow.modules.payments.dto.CreatePaymentOrderAllocationDto;
import com.astraval.coreflow.modules.payments.dto.CreateSellerPaymentDto;
import com.astraval.coreflow.modules.payments.model.PaymentOrderAllocations;
import com.astraval.coreflow.modules.payments.model.Payments;
import com.astraval.coreflow.modules.payments.repo.PaymentOrderAllocationRepository;
import com.astraval.coreflow.modules.payments.repo.PaymentRepository;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssets;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssetsRepository;
import com.astraval.coreflow.modules.vendor.VendorService;
import com.astraval.coreflow.modules.vendor.Vendors;

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

    @Autowired
    private UserCompanyAssetsRepository userCompanyAssetsRepository;

    @Autowired
    private VendorService vendorService;

    @Transactional
    public Long createSellerPayment(Long companyId, CreateSellerPaymentDto request) {
        Companies sellerCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Get company assets from view
        UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
        if (companyAssets == null) {
            throw new RuntimeException("No assets found for company");
        }

        // Check customer belongs to company
        if (companyAssets.getCustomers() == null
                || !Arrays.asList(companyAssets.getCustomers()).contains(request.getCustomerId())) {
            throw new RuntimeException("Customer does not belong to the requesting company");
        }

        Customers customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Payments payment = new Payments();
        payment.setSellerCompany(sellerCompany);
        payment.setCustomers(customer);
        
        if (customer.getCustomerCompany() != null) {
            payment.setBuyerCompany(customer.getCustomerCompany());
            // Find vendor relationship if customer has a company
            Long customersVendorCompanyId = customer.getCustomerCompany().getCompanyId();
            Vendors buyerVendor = vendorService.getBuyersVendorId(companyId, customersVendorCompanyId);
            payment.setVendors(buyerVendor);
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

    private void setPaymentDetails(Payments payment, CreatePaymentDto paymentDetails) {
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
    
    @Transactional
    private void createOrderAllocations(Payments payment, java.util.List<CreatePaymentOrderAllocationDto> allocations) {
        allocations.forEach(allocationDto -> {
            OrderDetails order = orderDetailsRepository.findById(allocationDto.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + allocationDto.getOrderId()));

            // Check if order belongs to the customer
            if (order.getCustomers() == null || !order.getCustomers().getCustomerId().equals(payment.getCustomers().getCustomerId())) {
                throw new RuntimeException(
                        "Order " + allocationDto.getOrderId() + 
                        " does not belong to customer " + payment.getCustomers().getCustomerName());
            }

            // order level payment amount checking logic.
            Double totalAmountPaidToOrder = order.getPaidAmount() + allocationDto.getAmountApplied();
            Double amountNeedToPay = order.getTotalAmount() - order.getPaidAmount();
            
            // Check if order is already fully paid
            if (Math.abs(order.getPaidAmount() - order.getTotalAmount()) < 0.0001) {
                throw new RuntimeException(
                        "Order " + allocationDto.getOrderId() +
                                " Already Fully Paid.");
            }
            
            // Check if payment exceeds remaining amount
            if (totalAmountPaidToOrder > order.getTotalAmount()) {
                throw new RuntimeException(
                        "Payment exceeds remaining amount for Order " + allocationDto.getOrderId() +
                                ". Amount needed: " + amountNeedToPay +
                                ", Amount trying to pay: " + allocationDto.getAmountApplied());
            }
            order.setPaidAmount(totalAmountPaidToOrder);
            if (order.getTotalAmount().equals(totalAmountPaidToOrder)) {
                order.setOrderStatus(OrderStatus.getOrderPayed());
            }
            
            PaymentOrderAllocations allocation = new PaymentOrderAllocations();
            allocation.setPayments(payment);
            allocation.setOrderDetails(order);
            allocation.setAmountApplied(allocationDto.getAmountApplied());
            allocation.setAllocationDate(allocationDto.getAllocationDate() != null ? 
                    allocationDto.getAllocationDate() : LocalDateTime.now());
            allocation.setAllocationRemarks(allocationDto.getAllocationRemarks());
            
            orderDetailsRepository.save(order);
            paymentOrderAllocationRepository.save(allocation);
        });
    }
}