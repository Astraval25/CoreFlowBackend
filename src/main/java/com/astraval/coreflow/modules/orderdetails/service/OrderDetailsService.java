package com.astraval.coreflow.modules.orderdetails.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.OrderStatus;
import com.astraval.coreflow.modules.orderdetails.dto.OrderDetailsWithItems;
import com.astraval.coreflow.modules.orderdetails.dto.UnpaidOrderDto;
import com.astraval.coreflow.modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.modules.orderitemsnapshot.OrderItemSnapshot;
import com.astraval.coreflow.modules.orderitemsnapshot.OrderItemSnapshotService;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetails;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetailsRepository;
import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.repo.OrderSnapshotRepository;

@Service
public class OrderDetailsService {
  
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    
    @Autowired
    private OrderSnapshotRepository orderSnapshotRepository;
    
    @Autowired
    private OrderItemSnapshotService orderItemSnapshotService;
    
    @Autowired
    private OrderItemDetailsRepository orderItemDetailsRepository;
    
    
    
    public OrderDetails getOrderDetailsByOrderId(Long companyId, Long orderId){
        return orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    public OrderDetailsWithItems getOrderDetailsWithItemsByOrderId(Long companyId, Long orderId){
        OrderDetails orderDetails = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        List<OrderItemDetails> orderItems = orderItemDetailsRepository.findByOrderId(orderId);
        
        return new OrderDetailsWithItems(orderDetails, orderItems);
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
    
    @Transactional
    public void updateOrderStatus(Long companyId, Long orderId, String newStatus) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setOrderStatus(newStatus);
        orderDetailsRepository.save(order);
    }
    
    @Transactional
    public void updateOrderStatusWithOrderSnapshot(Long companyId, Long orderId, String newStatus) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!OrderStatus.getOrderViewed().equals(order.getOrderStatus())) {  // before updating check the order is in "Viewed" status.
            throw new RuntimeException("Order status can only be updated from Open status");
        }
        
        // Create snapshot before updating status
        createOrderSnapshot(order);
        
        order.setOrderStatus(newStatus);
        orderDetailsRepository.save(order);
    }
    
    @Transactional
    private void createOrderSnapshot(OrderDetails order) {
        // Create order snapshot
        OrderSnapshot snapshot = new OrderSnapshot();
        snapshot.setOrderReference(order.getOrderId());
        snapshot.setOrderNumber(order.getOrderNumber());
        snapshot.setOrderDate(order.getOrderDate());
        snapshot.setSellerCompany(order.getSellerCompany());
        snapshot.setBuyerCompany(order.getBuyerCompany());
        snapshot.setCustomers(order.getCustomers());
        snapshot.setVendors(order.getVendors());
        snapshot.setTaxAmount(order.getTaxAmount());
        snapshot.setDiscountAmount(order.getDiscountAmount());
        snapshot.setDeliveryCharge(order.getDeliveryCharge());
        snapshot.setOrderAmount(order.getOrderAmount());
        snapshot.setTotalAmount(order.getTotalAmount());
        snapshot.setPaidAmount(order.getPaidAmount());
        snapshot.setOrderStatus(order.getOrderStatus());
        snapshot.setHasBill(order.getHasBill());
        snapshot.setIsActive(order.getIsActive());
        
        OrderSnapshot savedSnapshot = orderSnapshotRepository.save(snapshot);
        
        // Create order item snapshots
        orderItemDetailsRepository.findByOrderId(order.getOrderId()).forEach(orderItem -> {
            OrderItemSnapshot itemSnapshot = new OrderItemSnapshot();
            itemSnapshot.setOrderId(savedSnapshot.getOrderId());
            itemSnapshot.setItemId(orderItem.getItemId());
            itemSnapshot.setQuantity(orderItem.getQuantity());
            itemSnapshot.setBasePrice(orderItem.getBasePrice());
            itemSnapshot.setUpdatedPrice(orderItem.getUpdatedPrice());
            itemSnapshot.setItemTotal(orderItem.getItemTotal());
            itemSnapshot.setReadyStatus(orderItem.getReadyStatus());
            itemSnapshot.setStatus(orderItem.getStatus());
            itemSnapshot.setIsActive(orderItem.getIsActive());
            
            orderItemSnapshotService.createOrderItem(itemSnapshot);
        });
    }
    
    // -----------------------> Helper functions
    public String getNextSequenceNumber(Long companyId) {
        return orderDetailsRepository.generateOrderNumber(companyId);
    }

    public List<UnpaidOrderDto> getUnpaidOrdersByBuyerCompanyIdAndVendorId(Long buyerCompanyId, Long vendorId) {
        String orderStatus = OrderStatus.getOrderInvoiced();
        return orderDetailsRepository.findUnpaidOrdersByBuyerCompanyIdAndVendorId(buyerCompanyId, vendorId, orderStatus);
    }
}
