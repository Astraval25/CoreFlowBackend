package com.astraval.coreflow.modules.orderdetails;

import java.time.LocalDateTime;
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
import com.astraval.coreflow.modules.orderdetails.dto.CreateOrder;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetails;
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
    private CustomerRepository customerRepository;
    
    @Transactional
    public Long createOrder(Long companyId, CreateOrder createOrder) {
        Companies sellerCompany = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        Customers toCustomers = customerRepository.findById(createOrder.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
                
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
        String orderNumber = getNextSequenceNumber(companyId);
        orderDetails.setOrderNumber(orderNumber);
        
        OrderDetails savedOrder = orderDetailsRepository.save(orderDetails);
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
        orderDetailsRepository.save(savedOrder);
        
        
        return savedOrder.getOrderId();
    }

    // Helper functions...
    private String getNextSequenceNumber(Long companyId) {
        return orderDetailsRepository.generateOrderNumber(companyId);
    }
}
