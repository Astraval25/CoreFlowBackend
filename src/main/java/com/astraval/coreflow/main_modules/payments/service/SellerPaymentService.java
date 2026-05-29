package com.astraval.coreflow.main_modules.payments.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.astraval.coreflow.main_modules.companies.CompanyRepository;
import com.astraval.coreflow.main_modules.companylink.CompanyLink;
import com.astraval.coreflow.main_modules.companylink.CompanyLinkRepository;
import com.astraval.coreflow.main_modules.companyref.CompanyRefService;
import com.astraval.coreflow.main_modules.config.CompanyNumberSequenceRepository;
import com.astraval.coreflow.main_modules.customer.CustomerService;
import com.astraval.coreflow.main_modules.customer.Customers;
import com.astraval.coreflow.main_modules.filestorage.FileStorage;
import com.astraval.coreflow.main_modules.filestorage.FileStorageRepository;
import com.astraval.coreflow.main_modules.filestorage.FileStorageService;
import com.astraval.coreflow.main_modules.notification.NotificationService;
import com.astraval.coreflow.main_modules.orderdetails.OrderDetails;
import com.astraval.coreflow.main_modules.orderdetails.OrderStatus;
import com.astraval.coreflow.main_modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.main_modules.payments.PaymentStatus;
import com.astraval.coreflow.main_modules.payments.dto.CreatePaymentDto;
import com.astraval.coreflow.main_modules.payments.dto.CreatePaymentOrderAllocationDto;
import com.astraval.coreflow.main_modules.payments.dto.CreateSellerPaymentDto;
import com.astraval.coreflow.main_modules.payments.dto.PaymentProofUploadResponse;
import com.astraval.coreflow.main_modules.payments.dto.SellerPaymentSummaryDto;
import com.astraval.coreflow.main_modules.payments.dto.UpdatePaymentOrderAllocationDto;
import com.astraval.coreflow.main_modules.payments.dto.UpdateSellerPaymentDto;
import com.astraval.coreflow.main_modules.payments.model.PaymentOrderAllocations;
import com.astraval.coreflow.main_modules.payments.model.Payments;
import com.astraval.coreflow.main_modules.payments.repo.PaymentOrderAllocationRepository;
import com.astraval.coreflow.main_modules.payments.repo.PaymentRepository;
import com.astraval.coreflow.main_modules.vendor.Vendors;

@Service
public class SellerPaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentOrderAllocationRepository paymentOrderAllocationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CompanyLinkRepository customerVendorLinkRepository;

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

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CompanyRefService companyRefService;

    @Autowired
    private CompanyNumberSequenceRepository companyNumberSequenceRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Long createSellerPayment(Long companyId, CreateSellerPaymentDto request) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Customers customer = customerService.getCustomerById(companyId, request.getCustomerId());

        Payments payment = new Payments();
        payment.setCustomers(customer);

        CompanyLink customerVendorLink = customerVendorLinkRepository
                .findByCustomerCustomerIdAndIsActiveTrue(customer.getCustomerId())
                .orElse(null);
        if (customerVendorLink != null && customerVendorLink.getVendor() != null) {
            Vendors linkedVendor = customerVendorLink.getVendor();
            if (linkedVendor == null || linkedVendor.getCompany() == null) {
                throw new RuntimeException(
                        "Customer-vendor link is invalid for payments-received. " +
                                "customerId=" + customer.getCustomerId() +
                                ", receiverCompanyId=" + companyId);
            }

            Long linkedVendorCompanyId = linkedVendor.getCompany().getCompanyId();
            Long expectedVendorCompanyId = customerVendorLink.getCustomerCompany() != null
                    ? customerVendorLink.getCustomerCompany().getCompanyId()
                    : linkedVendorCompanyId;
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
        payment.setPaymentNumber(paymentService.getNextPaymentNumber(companyId));

        // Platform reference number
        payment.setPlatformRef(paymentRepository.generatePlatformPaymentRef());

        Payments savedPayment = paymentRepository.save(payment);

        // Company overlay for seller (payee)
        String sellerLocalNumber = companyNumberSequenceRepository.generateCompanyNumber(companyId, "PAYMENT_IN");
        companyRefService.createPaymentRef(companyId, savedPayment, sellerLocalNumber);

        // Company overlay for buyer (payer, if linked)
        Long buyerCompanyId = null;
        if (customerVendorLink != null && customerVendorLink.getCustomerCompany() != null) {
            buyerCompanyId = customerVendorLink.getCustomerCompany().getCompanyId();
        } else if (customer.getCustomerCompany() != null) {
            buyerCompanyId = customer.getCustomerCompany().getCompanyId();
        }
        if (buyerCompanyId != null) {
            String buyerLocalNumber = companyNumberSequenceRepository.generateCompanyNumber(buyerCompanyId, "PAYMENT_OUT");
            companyRefService.createPaymentRef(buyerCompanyId, savedPayment, buyerLocalNumber);

            if (savedPayment.getVendors() != null) {
                notificationService.createCompanyNotification(
                        companyId,
                        buyerCompanyId,
                        "New Seller Payment",
                        "A seller payment is recorded by " + customer.getCompany().getCompanyName(),
                        "SELLER_PAYMENT_CREATED",
                        "View Payments",
                        "/companies/" + buyerCompanyId + "/payments/sent",
                        null,
                        "VENDOR",
                        savedPayment.getVendors().getVendorId());
            }
        }

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
                row[3] != null ? (String) row[3] : null,  // payment_number
                row[4] != null ? ((Number) row[4]).doubleValue() : null, // amount
                (String) row[5],                         // customer_name
                (String) row[6],                         // mode_of_payment
                (String) row[7],                         // payment_status
                (Boolean) row[8],                        // is_active
                (String) row[9],                         // reference_number
                row[10] != null ? (String) row[10] : null, // platform_ref
                row[11] != null ? (String) row[11] : null  // local_payment_number
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
        final List<PaymentOrderAllocations> existingAllocations =
                paymentOrderAllocationRepository.findByPayments(payment);
        final Map<Long, PaymentOrderAllocations> existingById = new HashMap<>();
        for (PaymentOrderAllocations existing : existingAllocations) {
            if (existing.getPaymentOrderAllocationId() != null) {
                existingById.put(existing.getPaymentOrderAllocationId(), existing);
            }
        }

        final Set<Long> retainedExistingIds = new HashSet<>();
        final Set<OrderDetails> touchedOrders = new HashSet<>();
        final Set<Long> requestAllocationIds = new HashSet<>();
        double totalAllocated = 0.0;

        for (UpdatePaymentOrderAllocationDto allocationDto : allocations) {
            OrderDetails order = orderDetailsRepository.findById(allocationDto.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + allocationDto.getOrderId()));

            if (payment.getCustomers() != null &&
                    (order.getCustomers() == null ||
                            !order.getCustomers().getCustomerId().equals(payment.getCustomers().getCustomerId()))) {
                throw new RuntimeException(
                        "Order " + allocationDto.getOrderId() +
                                " does not belong to customer " + payment.getCustomers().getCustomerName());
            }

            PaymentOrderAllocations allocation;
            if (allocationDto.getPaymentOrderAllocationId() != null) {
                Long allocationId = allocationDto.getPaymentOrderAllocationId();
                if (!requestAllocationIds.add(allocationId)) {
                    throw new RuntimeException("Duplicate allocation id in request: " + allocationId);
                }
                allocation = existingById.get(allocationId);
                if (allocation == null) {
                    throw new RuntimeException("Payment allocation not found with ID: " + allocationId);
                }
                retainedExistingIds.add(allocationId);
                if (allocation.getOrderDetails() != null) {
                    touchedOrders.add(allocation.getOrderDetails());
                }
            } else {
                allocation = new PaymentOrderAllocations();
                allocation.setPayments(payment);
            }

            allocation.setOrderDetails(order);
            allocation.setAmountApplied(allocationDto.getAmountApplied());
            allocation.setAllocationDate(allocationDto.getAllocationDate() != null
                    ? allocationDto.getAllocationDate()
                    : LocalDateTime.now());
            allocation.setAllocationRemarks(allocationDto.getAllocationRemarks());
            paymentOrderAllocationRepository.save(allocation);

            totalAllocated += allocationDto.getAmountApplied();
            touchedOrders.add(order);
        }

        if (totalAllocated - payment.getAmount() > 0.0001) {
            throw new RuntimeException(
                    "Total allocated amount (" + totalAllocated + ") exceeds payment amount (" + payment.getAmount() + ")");
        }

        for (PaymentOrderAllocations existing : existingAllocations) {
            Long existingId = existing.getPaymentOrderAllocationId();
            if (existingId != null && !retainedExistingIds.contains(existingId)) {
                touchedOrders.add(existing.getOrderDetails());
                paymentOrderAllocationRepository.delete(existing);
            }
        }

        for (OrderDetails touchedOrder : touchedOrders) {
            if (touchedOrder == null) continue;
            Double latestTotalPaid = paymentOrderAllocationRepository.getTotalPaidAmountForOrder(touchedOrder.getOrderId());
            if (latestTotalPaid != null && latestTotalPaid - touchedOrder.getTotalAmount() > 0.0001) {
                throw new RuntimeException(
                        "Payment exceeds remaining amount for Order " + touchedOrder.getOrderId() +
                                ". Order total: " + touchedOrder.getTotalAmount() +
                                ", total allocated: " + latestTotalPaid);
            }
            recalculateOrderPaymentState(touchedOrder);
        }
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
