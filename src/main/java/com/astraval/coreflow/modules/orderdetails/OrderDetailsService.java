package com.astraval.coreflow.modules.orderdetails;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.companies.CompanyRepository;
import com.astraval.coreflow.modules.customer.CustomerRepository;
import com.astraval.coreflow.modules.customer.Customers;
import com.astraval.coreflow.modules.items.ItemRepository;
import com.astraval.coreflow.modules.orderauditlog.OrderAuditLogService;
import com.astraval.coreflow.modules.orderdetails.dto.CreateOrder;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetailsMapper;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetailsService;

@Service
public class OrderDetailsService {
  
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private OrderDetailsMapper orderDetailsMapper;
    
    @Autowired
    private OrderItemDetailsService orderItemDetailsService;
    
    @Autowired
    private OrderItemDetailsMapper orderItemDetailsMapper;
    
    @Autowired
    private OrderAuditLogService orderAuditLogService;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Transactional
    public Long createOrder(Long companyId, CreateOrder createOrder) {
        Companies sellerCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        Customers toCustomers = customerRepository.findById(createOrder.getCustomId())
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
                
        OrderDetails orderDetails = orderDetailsMapper.toOrderDetails(createOrder);
        orderDetails.setSellerCompany(sellerCompany);
        orderDetails.setBuyerCompany(toCustomers.getCustomerCompany());
        orderDetails.setCustomers(toCustomers);
        orderDetails.setOrderDate(LocalDateTime.now());
        
        // Generate order number
        String orderNumber = getNextSequenceNumber(companyId);
        orderDetails.setOrderNumber(orderNumber);
        
        OrderDetails savedOrder = orderDetailsRepository.save(orderDetails);
        
        // Create order items
        // createOrder.getCreateOrderItems().forEach(createOrderItem -> {
            
        // });
        
        // Log order creation
        // orderAuditLogService.logOrderCreation(savedOrder.getOrderId());
        
        return savedOrder.getOrderId();
    }

    private String getNextSequenceNumber(Long companyId) {
        return orderDetailsRepository.generateOrderNumber(companyId);
    }
}
