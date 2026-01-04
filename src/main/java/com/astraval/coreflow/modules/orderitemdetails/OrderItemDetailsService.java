package com.astraval.coreflow.modules.orderitemdetails;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.orderauditlog.OrderAuditLogService;

@Service
public class OrderItemDetailsService {
  
    @Autowired
    private OrderItemDetailsRepository orderItemDetailsRepository;
    
    @Autowired
    private OrderAuditLogService orderAuditLogService;
    
    @Transactional
    public OrderItemDetails createOrderItem(OrderItemDetails orderItemDetails) {
        OrderItemDetails savedItem = orderItemDetailsRepository.save(orderItemDetails);
        
        // Log item creation
        orderAuditLogService.logOrderItemCreation(savedItem.getOrderId(), savedItem.getOrderItemId());
        
        return savedItem;
    }
    
    public List<OrderItemDetails> getOrderItemsByOrderId(Long orderId) {
        return orderItemDetailsRepository.findByOrderIdAndIsActive(orderId, true);
    }
    
    @Transactional
    public void updateOrderItemQuantity(Long orderItemId, Integer newQuantity) {
        OrderItemDetails orderItem = orderItemDetailsRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found"));
        
        Integer oldQuantity = orderItem.getQuantity();
        orderItem.setQuantity(newQuantity);
        orderItemDetailsRepository.save(orderItem);
        
        // Log quantity change
        orderAuditLogService.logOrderItemQuantityChange(orderItem.getOrderId(), orderItemId, 
                oldQuantity.toString(), newQuantity.toString());
    }
    
    @Transactional
    public void updateOrderItemPrice(Long orderItemId, Double newPrice) {
        OrderItemDetails orderItem = orderItemDetailsRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found"));
        
        Double oldPrice = orderItem.getUpdatedPrice();
        orderItem.setUpdatedPrice(newPrice);
        orderItemDetailsRepository.save(orderItem);
        
        // Log price change
        orderAuditLogService.logOrderItemPriceChange(orderItem.getOrderId(), orderItemId, 
                oldPrice.toString(), newPrice.toString());
    }
}
