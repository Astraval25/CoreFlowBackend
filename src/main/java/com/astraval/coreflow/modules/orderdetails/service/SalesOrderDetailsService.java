package com.astraval.coreflow.modules.orderdetails.service;

import java.time.LocalDateTime;
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

@Service
public class SalesOrderDetailsService {
  
    @Autowired
    private SalesOrderDetailsRepository salesOrderDetailsRepository;
    
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
    public Long createSalesOrder(Long companyId, CreateSalesOrder createOrder) {
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
    
    public OrderDetails getOrderDetailsByOrderId(Long companyId, Long orderId){
        return salesOrderDetailsRepository
                .findByOrderIdAndSellerCompany_CompanyId(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    @Transactional
    public void deleteOrder(Long companyId, Long orderId) {
        OrderDetails order = salesOrderDetailsRepository
                .findByOrderIdAndSellerCompany_CompanyId(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        salesOrderDetailsRepository.delete(order);
    }
    
    @Transactional
    public void deactivateOrder(Long companyId, Long orderId) {
        OrderDetails order = salesOrderDetailsRepository
                .findByOrderIdAndSellerCompany_CompanyId(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setIsActive(false);
        salesOrderDetailsRepository.save(order);
    }
    
    @Transactional
    public void activateOrder(Long companyId, Long orderId) {
        OrderDetails order = salesOrderDetailsRepository
                .findByOrderIdAndSellerCompany_CompanyId(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setIsActive(true);
        salesOrderDetailsRepository.save(order);
    }
    
    // -----------------------> Helper functions
    private String getNextSequenceNumber(Long companyId) {
        return salesOrderDetailsRepository.generateOrderNumber(companyId);
    }
}
