package com.astraval.coreflow.modules.payments.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerVendorLink;
import com.astraval.coreflow.modules.customer.CustomerVendorLinkRepository;
import com.astraval.coreflow.modules.customer.CustomerService;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.filestorage.FileStorage;
import com.astraval.coreflow.modules.filestorage.FileStorageRepository;
import com.astraval.coreflow.modules.filestorage.FileStorageService;
import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.OrderStatus;
import com.astraval.coreflow.modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.modules.payments.PaymentStatus;
import com.astraval.coreflow.modules.payments.dto.CreatePaymentDto;
import com.astraval.coreflow.modules.payments.dto.CreatePaymentOrderAllocationDto;
import com.astraval.coreflow.modules.payments.dto.CreateSellerPaymentDto;
import com.astraval.coreflow.modules.payments.dto.PaymentProofUploadResponse;
import com.astraval.coreflow.modules.payments.dto.SellerPaymentSummaryDto;
import com.astraval.coreflow.modules.payments.dto.UpdatePaymentOrderAllocationDto;
import com.astraval.coreflow.modules.payments.dto.UpdateSellerPaymentDto;
import com.astraval.coreflow.modules.payments.model.PaymentOrderAllocations;
import com.astraval.coreflow.modules.payments.model.Payments;
import com.astraval.coreflow.modules.payments.repo.PaymentOrderAllocationRepository;
import com.astraval.coreflow.modules.payments.repo.PaymentRepository;
import com.astraval.coreflow.modules.vendor.Vendors;
import org.springframework.web.multipart.MultipartFile;

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
    private CustomerService customerService;

    @Autowired
    private CustomerVendorLinkRepository customerVendorLinkRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FileStorageRepository fileStorageRepository;

    @Autowired
    private PaymentProofTextExtractor paymentProofTextExtractor;

    @Autowired
    private PartnerBalanceService partnerBalanceService;

    @Transactional
    public Long createSellerPayment(Long companyId, CreateSellerPaymentDto request) {
        Companies receiverComp = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Customers customer = customerService.getCustomerById(companyId, request.getCustomerId());

        Payments payment = new Payments();
        payment.setReceiverComp(receiverComp);
        payment.setCustomers(customer);
        
        if (customer.getCustomerCompany() != null) {
            payment.setSenderComp(customer.getCustomerCompany());
            Long expectedVendorCompanyId = customer.getCustomerCompany().getCompanyId();

            CustomerVendorLink customerVendorLink = customerVendorLinkRepository
                    .findByCustomerCustomerId(customer.getCustomerId())
                    .orElseThrow(() -> new RuntimeException(
                            "Customer-vendor link not found for payments-received. " +
                                    "customerId=" + customer.getCustomerId() +
                                    ", receiverCompanyId=" + companyId +
                                    ", expectedVendorCompanyId=" + expectedVendorCompanyId));

            Vendors linkedVendor = customerVendorLink.getVendor();
            if (linkedVendor == null || linkedVendor.getCompany() == null) {
                throw new RuntimeException(
                        "Customer-vendor link is invalid for payments-received. " +
                                "customerId=" + customer.getCustomerId() +
                                ", receiverCompanyId=" + companyId);
            }

            Long linkedVendorCompanyId = linkedVendor.getCompany().getCompanyId();
            if (!expectedVendorCompanyId.equals(linkedVendorCompanyId)) {
                throw new RuntimeException(
                        "Customer-vendor link mismatch for payments-received. " +
                                "customerId=" + customer.getCustomerId() +
                                ", expectedVendorCompanyId=" + expectedVendorCompanyId +
                                ", linkedVendorCompanyId=" + linkedVendorCompanyId +
                                ", receiverCompanyId=" + companyId);
            }

            payment.setVendors(linkedVendor);
        }

        // Create Payment Details
        setPaymentDetails(payment, request.getPaymentDetails());
        payment.setPaymentStatus(PaymentStatus.getPaid());

        Payments savedPayment = paymentRepository.save(payment);
        
        // Create order allocations if provided
        if (request.getPaymentDetails().getOrderAllocations() != null) {
            createOrderAllocations(savedPayment, request.getPaymentDetails().getOrderAllocations());
        }
        partnerBalanceService.refreshDueAmountsForPayment(savedPayment);

        return savedPayment.getPaymentId();
    }

    private void setPaymentDetails(Payments payment, CreatePaymentDto paymentDetails) {
        payment.setAmount(paymentDetails.getAmount());
        payment.setPaymentDate(paymentDetails.getPaymentDate());
        payment.setModeOfPayment(paymentDetails.getModeOfPayment());
        payment.setReferenceNumber(paymentDetails.getReferenceNumber());
        payment.setPaymentRemarks(paymentDetails.getPaymentRemarks());
        if (paymentDetails.getPaymentProofFsId() != null && !paymentDetails.getPaymentProofFsId().isBlank()) {
            FileStorage file = fileStorageRepository.findByFsId(paymentDetails.getPaymentProofFsId())
                    .orElseThrow(() -> new RuntimeException("Payment proof file not found"));
            payment.setPaymentProofFile(file);
        }
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

    public List<SellerPaymentSummaryDto> getSellerPaymentSummaryByCompanyId(Long companyId) {
        List<Object[]> results = paymentRepository.findPayeePaymentSummaryByCompanyIdNative(companyId);
        return results.stream()
                .map(this::mapToSellerPaymentSummaryDto)
                .collect(Collectors.toList());
    }

    private SellerPaymentSummaryDto mapToSellerPaymentSummaryDto(Object[] row) {
        return new SellerPaymentSummaryDto(
                ((Number) row[0]).longValue(),           // payment_id
                (LocalDateTime) row[1],                  // payment_date
                (String) row[2],                         // order_ids
                row[3] != null ? ((Number) row[3]).longValue() : null,  // payment_number
                row[4] != null ? ((Number) row[4]).doubleValue() : null, // amount
                (String) row[5],                         // customer_name
                (String) row[6],                         // mode_of_payment
                (String) row[7],                         // payment_status
                (Boolean) row[8],                        // is_active
                (String) row[9]                          // reference_number
        );
    }

    @Transactional
    public void updateSellerPayment(Long companyId, Long paymentId, UpdateSellerPaymentDto request) {
        Payments payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        
        if (payment.getReceiverComp() == null || !payment.getReceiverComp().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Payment does not belong to the requesting company");
        }
        
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setModeOfPayment(request.getModeOfPayment());
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setPaymentRemarks(request.getPaymentRemarks());

        if (request.getPaymentProofFsId() != null && !request.getPaymentProofFsId().isBlank()) {
            FileStorage file = fileStorageRepository.findByFsId(request.getPaymentProofFsId())
                    .orElseThrow(() -> new RuntimeException("Payment proof file not found"));
            payment.setPaymentProofFile(file);
        }
        
        paymentRepository.save(payment);
        
        if (request.getOrderAllocations() != null) {
            updateOrderAllocations(payment, request.getOrderAllocations());
        }
        partnerBalanceService.refreshDueAmountsForPayment(payment);
    }

    @Transactional
    private void updateOrderAllocations(Payments payment, List<UpdatePaymentOrderAllocationDto> allocations) {
        allocations.forEach(allocationDto -> {
            OrderDetails order = orderDetailsRepository.findById(allocationDto.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + allocationDto.getOrderId()));
            
            if (payment.getCustomers() != null) {
                if (order.getCustomers() == null || !order.getCustomers().getCustomerId().equals(payment.getCustomers().getCustomerId())) {
                    throw new RuntimeException(
                            "Order " + allocationDto.getOrderId() + 
                            " does not belong to customer " + payment.getCustomers().getCustomerName());
                }
            }
            
            PaymentOrderAllocations allocation;
            OrderDetails previousOrder = null;
            Double previousAppliedAmount = 0.0;
            
            if (allocationDto.getPaymentOrderAllocationId() != null) {
                allocation = paymentOrderAllocationRepository.findById(allocationDto.getPaymentOrderAllocationId())
                        .orElseThrow(() -> new RuntimeException("Payment allocation not found with ID: " + allocationDto.getPaymentOrderAllocationId()));
                
                if (!allocation.getPayments().getPaymentId().equals(payment.getPaymentId())) {
                    throw new RuntimeException("Payment allocation does not belong to this payment");
                }

                previousOrder = allocation.getOrderDetails();
                previousAppliedAmount = allocation.getAmountApplied() != null ? allocation.getAmountApplied() : 0.0;
            } else {
                allocation = new PaymentOrderAllocations();
                allocation.setPayments(payment);
            }

            Double currentPaidForTarget = paymentOrderAllocationRepository.getTotalPaidAmountForOrder(order.getOrderId());
            if (currentPaidForTarget == null) {
                currentPaidForTarget = 0.0;
            }

            Double nextPaidForTarget = currentPaidForTarget + allocationDto.getAmountApplied();
            if (previousOrder != null && previousOrder.getOrderId().equals(order.getOrderId())) {
                nextPaidForTarget = currentPaidForTarget - previousAppliedAmount + allocationDto.getAmountApplied();
            }

            if (nextPaidForTarget > order.getTotalAmount()) {
                throw new RuntimeException(
                        "Payment exceeds remaining amount for Order " + allocationDto.getOrderId() +
                                ". Amount needed: " + (order.getTotalAmount() - currentPaidForTarget) +
                                ", Amount trying to pay: " + allocationDto.getAmountApplied());
            }
            
            allocation.setOrderDetails(order);
            allocation.setAmountApplied(allocationDto.getAmountApplied());
            allocation.setAllocationDate(allocationDto.getAllocationDate() != null ? 
                    allocationDto.getAllocationDate() : LocalDateTime.now());
            allocation.setAllocationRemarks(allocationDto.getAllocationRemarks());
            
            paymentOrderAllocationRepository.save(allocation);

            recalculateOrderPaymentState(order);
            if (previousOrder != null && !previousOrder.getOrderId().equals(order.getOrderId())) {
                recalculateOrderPaymentState(previousOrder);
            }
        });
    }

    @Transactional
    public PaymentProofUploadResponse uploadPaymentProof(Long companyId, Long paymentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Payment proof file is required");
        }
        
        Payments payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));
        
        if (payment.getReceiverComp() == null || !payment.getReceiverComp().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Payment does not belong to the requesting company");
        }
        
        try {
            FileStorage fileStorage = fileStorageService.saveFile(file, "PAYMENT_PROOF", paymentId.toString());
            FileStorage savedFile = fileStorageRepository.save(fileStorage);
            
            payment.setPaymentProofFile(savedFile);
            paymentRepository.save(payment);

            String extractedText = paymentProofTextExtractor.extractText(
                    savedFile.getFilePath(),
                    savedFile.getMimeType());
            String transactionId = paymentProofTextExtractor.extractTransactionId(extractedText);
            Double amount = paymentProofTextExtractor.extractAmount(extractedText);

            return new PaymentProofUploadResponse(savedFile.getFsId(), transactionId, amount, extractedText);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload payment proof: " + e.getMessage(), e);
        }
    }

    private void recalculateOrderPaymentState(OrderDetails order) {
        Double totalPaid = paymentOrderAllocationRepository.getTotalPaidAmountForOrder(order.getOrderId());
        if (totalPaid == null) {
            totalPaid = 0.0;
        }
        order.setPaidAmount(totalPaid);

        if (Math.abs(order.getTotalAmount() - totalPaid) < 0.0001) {
            order.setOrderStatus(OrderStatus.getOrderPayed());
        } else {
            order.setOrderStatus(OrderStatus.getOrderInvoiced());
        }

        orderDetailsRepository.save(order);
    }
}
