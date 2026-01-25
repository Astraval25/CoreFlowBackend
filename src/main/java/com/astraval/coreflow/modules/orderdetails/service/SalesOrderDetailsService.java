package com.astraval.coreflow.modules.orderdetails.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.items.repo.ItemRepository;
import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.dto.CreateSalesOrder;
import com.astraval.coreflow.modules.orderdetails.dto.SalesOrderSummaryDto;
import com.astraval.coreflow.modules.orderdetails.mapper.OrderDetailsMapper;
import com.astraval.coreflow.modules.orderdetails.repo.SalesOrderDetailsRepository;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetails;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetailsService;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssets;
import com.astraval.coreflow.modules.orderdetails.dto.UpdateSalesOrder;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssets;
import com.astraval.coreflow.modules.usercompmap.UserCompanyAssetsRepository;

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
    private UserCompanyAssetsRepository userCompanyAssetsRepository;
    
    @Transactional
    public Long createSalesOrder(Long companyId, CreateSalesOrder createOrder) {
        
        // Access Validation
        // 1. check the companyId is exist
        Companies sellerCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        // Get company assets from view
        UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
        if (companyAssets == null) {
            throw new RuntimeException("No assets found for company");
        }
        
        // 2. check the customerId exists and belongs to the requesting company
        if (companyAssets.getCustomers() == null || !Arrays.asList(companyAssets.getCustomers()).contains(createOrder.getCustomerId())) {
            throw new RuntimeException("Customer does not belong to the requesting company");
        }
        
        Customers toCustomers = customerRepository.findById(createOrder.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // 3. check the orderItems.items exist and belong to the requesting company 
        createOrder.getOrderItems().forEach(orderItem -> {
            if (companyAssets.getItems() == null || !Arrays.asList(companyAssets.getItems()).contains(orderItem.getItemId())) {
                throw new RuntimeException("Item does not belong to the requesting company: " + orderItem.getItemId());
            }
        });
        // Access Validation Done if all ok then only allow to create.
                
        OrderDetails orderDetails = orderDetailsMapper.toOrderDetails(createOrder);
        orderDetails.setSellerCompany(sellerCompany);
        orderDetails.setBuyerCompany(toCustomers.getCustomerCompany());
        orderDetails.setCustomers(toCustomers);
        orderDetails.setOrderDate(LocalDateTime.now());
        orderDetails.setDeliveryCharge(createOrder.getDeliveryCharge());
        orderDetails.setDiscountAmount(createOrder.getDiscountAmount());
        orderDetails.setTaxAmount(createOrder.getTaxAmount());
        orderDetails.setHasBill(createOrder.isHasBill());
        
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
            orderItem.setBasePrice(item.getSalesPrice() != null ? item.getSalesPrice() : item.getPurchasePrice());
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
        salesOrderDetailsRepository.save(savedOrder);
        
        toCustomers.setDueAmount(toCustomers.getDueAmount() + totalAmount);
        customerRepository.save(toCustomers);
        
        return savedOrder.getOrderId();
    }
    
    
    public List<SalesOrderSummaryDto> getOrderSummaryByCompanyId(Long companyId) {
      return salesOrderDetailsRepository.findOrdersByCompanyId(companyId);
    }
    
    @Transactional
    public void updateSalesOrder(Long companyId, Long orderId, UpdateSalesOrder updateOrder) {
        // Validation
        Companies sellerCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        OrderDetails existingOrder = salesOrderDetailsRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!existingOrder.getSellerCompany().getCompanyId().equals(companyId)) {
            throw new RuntimeException("Order does not belong to the requesting company");
        }
        
        UserCompanyAssets companyAssets = userCompanyAssetsRepository.findByCompanyId(companyId);
        if (companyAssets == null) {
            throw new RuntimeException("No assets found for company");
        }
        
        if (companyAssets.getCustomers() == null || !Arrays.asList(companyAssets.getCustomers()).contains(updateOrder.getCustomerId())) {
            throw new RuntimeException("Customer does not belong to the requesting company");
        }
        
        updateOrder.getOrderItems().forEach(orderItem -> {
            if (companyAssets.getItems() == null || !Arrays.asList(companyAssets.getItems()).contains(orderItem.getItemId())) {
                throw new RuntimeException("Item does not belong to the requesting company: " + orderItem.getItemId());
            }
        });
        
        Customers toCustomers = customerRepository.findById(updateOrder.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Update order details
        existingOrder.setCustomers(toCustomers);
        existingOrder.setBuyerCompany(toCustomers.getCustomerCompany());
        existingOrder.setDeliveryCharge(updateOrder.getDeliveryCharge());
        existingOrder.setDiscountAmount(updateOrder.getDiscountAmount());
        existingOrder.setTaxAmount(updateOrder.getTaxAmount());
        existingOrder.setHasBill(updateOrder.isHasBill());
        
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
            orderItem.setBasePrice(item.getSalesPrice() != null ? item.getSalesPrice() : item.getPurchasePrice());
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
        
        // Adjust customer due amount
        toCustomers.setDueAmount(toCustomers.getDueAmount() - existingOrder.getTotalAmount() + totalAmount);
        customerRepository.save(toCustomers);
        
        existingOrder.setOrderAmount(orderAmount);
        existingOrder.setTotalAmount(totalAmount);
        salesOrderDetailsRepository.save(existingOrder);
    }
    
}
