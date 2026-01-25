package com.astraval.coreflow.modules.orderdetails.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.repo.OrderDetailsRepository;

@Service
public class OrderDetailsService {
  
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    
    
    
    public OrderDetails getOrderDetailsByOrderId(Long companyId, Long orderId){
        return orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    @Transactional
    public void deleteOrder(Long companyId, Long orderId) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        orderDetailsRepository.delete(order);
    }
    
    @Transactional
    public void deactivateOrder(Long companyId, Long orderId) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setIsActive(false);
        orderDetailsRepository.save(order);
    }
    
    @Transactional
    public void activateOrder(Long companyId, Long orderId) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setIsActive(true);
        orderDetailsRepository.save(order);
    }
    
    // -----------------------> Helper functions
    public String getNextSequenceNumber(Long companyId) {
        return orderDetailsRepository.generateOrderNumber(companyId);
    }
}
