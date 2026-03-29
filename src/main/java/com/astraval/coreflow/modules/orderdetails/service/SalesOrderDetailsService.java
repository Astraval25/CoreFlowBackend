package com.astraval.coreflow.modules.orderdetails.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.OrderStatus;
import com.astraval.coreflow.modules.orderdetails.dto.CreateSalesOrder;
import com.astraval.coreflow.modules.orderdetails.dto.SalesOrderSummaryDto;
import com.astraval.coreflow.modules.orderdetails.mapper.OrderDetailsMapper;
import com.astraval.coreflow.modules.orderdetails.repo.SalesOrderDetailsRepository;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetails;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetailsService;
import com.astraval.coreflow.modules.orderdetails.dto.UpdateSalesOrder;
import com.astraval.coreflow.modules.payments.service.PartnerBalanceService;
import com.astraval.coreflow.modules.vendor.VendorService;
import com.astraval.coreflow.modules.vendor.Vendors;
import com.astraval.coreflow.modules.companyref.CompanyRefService;
import com.astraval.coreflow.modules.config.CompanyNumberSequenceRepository;
import com.astraval.coreflow.modules.orderdetails.repo.OrderDetailsRepository;

@Service
public class SalesOrderDetailsService {
  
    @Autowired
    private SalesOrderDetailsRepository salesOrderDetailsRepository;
    
    @Autowired
    private OrderDetailsService orderDetailsService;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private OrderDetailsMapper orderDetailsMapper;
    
    @Autowired
    private OrderItemDetailsService orderItemDetailsService;
        
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private VendorService vendorService;

    @Autowired
    private PartnerBalanceService partnerBalanceService;

    @Autowired
    private CompanyRefService companyRefService;

    @Autowired
    private CompanyNumberSequenceRepository companyNumberSequenceRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    
    @Transactional
    public Long createSalesOrder(Long companyId, CreateSalesOrder createOrder) {
        
        // Access Validation
        // 1. check the companyId is exist
        companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        Customers toCustomers = customerRepository.findById(createOrder.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        // Access Validation Done if all ok then only allow to create.

        OrderDetails orderDetails = orderDetailsMapper.toOrderDetails(createOrder);
        // Main id setting...
        orderDetails.setCustomers(toCustomers);
        if(toCustomers.getCustomerCompany() != null){
            Long customersVendorCompanyId = toCustomers.getCustomerCompany().getCompanyId();
            Vendors buyerVendor = vendorService.getBuyerVendorId(customersVendorCompanyId, companyId);
            orderDetails.setVendors(buyerVendor);
        }
        
        LocalDateTime orderDate = createOrder.getOrderDate() != null
                ? createOrder.getOrderDate()
                : LocalDateTime.now();
        orderDetails.setOrderDate(orderDate);
        orderDetails.setDeliveryCharge(createOrder.getDeliveryCharge());
        orderDetails.setDiscountAmount(createOrder.getDiscountAmount());
        orderDetails.setTaxAmount(createOrder.getTaxAmount());
        orderDetails.setHasBill(createOrder.isHasBill());
        orderDetails.setOrderStatus(OrderStatus.getOrderViewed()); // Set the order status to "Order Viewed".
        
        // Generate order number
        String orderNumber = orderDetailsService.getNextSequenceNumber(companyId);
        orderDetails.setOrderNumber(orderNumber);
        
        OrderDetails savedOrder = salesOrderDetailsRepository.save(orderDetails);
        AtomicReference<Double> orderTotalAmount = new AtomicReference<>(0.0);
        // Create order items
        createOrder.getOrderItems().forEach(newOrderItem -> {
            OrderItemDetails orderItem = new OrderItemDetails();
            Items item = itemRepository.findById(newOrderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            orderItem.setOrderId(savedOrder.getOrderId());
            orderItem.setItemId(item);
            orderItem.setQuantity(newOrderItem.getQuantity());
            orderItem.setBasePrice(item.getBaseSalesPrice() != null ? item.getBaseSalesPrice() : item.getBasePurchasePrice());
            orderItem.setUpdatedPrice(newOrderItem.getUpdatedPrice());
            orderItem.setItemTotal(newOrderItem.getQuantity() * newOrderItem.getUpdatedPrice());
            orderItem.setReadyStatus(0.0);
            orderItem.setStatus(null); // as of now no need for item level status tracking
            orderTotalAmount.updateAndGet(current -> current + orderItem.getItemTotal());
            orderItemDetailsService.createOrderItem(orderItem);
        });
        
        // Update order total amount
        Double orderAmount = orderTotalAmount.get() + createOrder.getDeliveryCharge();
        Double totalAmount = orderAmount - createOrder.getDiscountAmount() + createOrder.getTaxAmount();
        savedOrder.setOrderAmount(orderAmount);
        savedOrder.setTotalAmount(totalAmount);

        // Platform reference number
        savedOrder.setPlatformRef(orderDetailsRepository.generatePlatformOrderRef());
        salesOrderDetailsRepository.save(savedOrder);

        // Company overlay for seller
        String sellerLocalNumber = companyNumberSequenceRepository.generateCompanyNumber(companyId, "SALES_ORDER");
        companyRefService.createOrderRef(companyId, savedOrder, sellerLocalNumber);

        // Company overlay for buyer (if linked)
        if (toCustomers.getCustomerCompany() != null) {
            Long buyerCompanyId = toCustomers.getCustomerCompany().getCompanyId();
            String buyerLocalNumber = companyNumberSequenceRepository.generateCompanyNumber(buyerCompanyId, "PURCHASE_ORDER");
            companyRefService.createOrderRef(buyerCompanyId, savedOrder, buyerLocalNumber);
        }

        partnerBalanceService.refreshDueAmountsForOrder(savedOrder);

        return savedOrder.getOrderId();
    }
    
    
    public List<SalesOrderSummaryDto> getOrderSummaryByCompanyId(Long companyId) {
      return salesOrderDetailsRepository.findOrdersByCompanyId(companyId);
    }
    
    @Transactional
    public void updateSalesOrder(Long companyId, Long orderId, UpdateSalesOrder updateOrder) {
        // Validation
        companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        OrderDetails existingOrder = salesOrderDetailsRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (existingOrder.getSellerCompany() == null
                || !existingOrder.getSellerCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Order does not belong to the requesting company");
        }

        Long previousCustomerId = existingOrder.getCustomers() != null
                ? existingOrder.getCustomers().getCustomerId()
                : null;
        Long previousVendorId = existingOrder.getVendors() != null
                ? existingOrder.getVendors().getVendorId()
                : null;
        
        Customers toCustomers = customerRepository.findById(updateOrder.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Update order details
        existingOrder.setCustomers(toCustomers);
        if (toCustomers.getCustomerCompany() != null) {
            Long customersVendorCompanyId = toCustomers.getCustomerCompany().getCompanyId();
            Vendors buyerVendor = vendorService.getBuyerVendorId(customersVendorCompanyId, companyId);
            existingOrder.setVendors(buyerVendor);
        } else {
            existingOrder.setVendors(null);
        }
        existingOrder.setDeliveryCharge(updateOrder.getDeliveryCharge());
        existingOrder.setDiscountAmount(updateOrder.getDiscountAmount());
        existingOrder.setTaxAmount(updateOrder.getTaxAmount());
        existingOrder.setHasBill(updateOrder.isHasBill());
        if (updateOrder.getOrderDate() != null) {
            existingOrder.setOrderDate(updateOrder.getOrderDate());
        }
        
        // Delete existing order items
        orderItemDetailsService.deleteOrderItemsByOrderId(orderId);
        
        // Create new order items
        AtomicReference<Double> orderTotalAmount = new AtomicReference<>(0.0);
        updateOrder.getOrderItems().forEach(newOrderItem -> {
            OrderItemDetails orderItem = new OrderItemDetails();
            Items item = itemRepository.findById(newOrderItem.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            orderItem.setOrderId(existingOrder.getOrderId());
            orderItem.setItemId(item);
            orderItem.setQuantity(newOrderItem.getQuantity());
            orderItem.setBasePrice(item.getBaseSalesPrice() != null ? item.getBaseSalesPrice() : item.getBasePurchasePrice());
            orderItem.setUpdatedPrice(newOrderItem.getUpdatedPrice());
            orderItem.setItemTotal(newOrderItem.getQuantity() * newOrderItem.getUpdatedPrice());
            orderItem.setReadyStatus(0.0);
            orderItem.setStatus(null);
            orderTotalAmount.updateAndGet(current -> current + orderItem.getItemTotal());
            orderItemDetailsService.createOrderItem(orderItem);
        });
        
        // Update totals
        Double orderAmount = orderTotalAmount.get() + updateOrder.getDeliveryCharge();
        Double totalAmount = orderAmount - updateOrder.getDiscountAmount() + updateOrder.getTaxAmount();
        
        existingOrder.setOrderAmount(orderAmount);
        existingOrder.setTotalAmount(totalAmount);
        salesOrderDetailsRepository.save(existingOrder);
        partnerBalanceService.refreshDueAmounts(previousCustomerId, previousVendorId);
        partnerBalanceService.refreshDueAmountsForOrder(existingOrder);
    }
    
}
