package com.astraval.coreflow.modules.orderdetails.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.OrderStatus;
import com.astraval.coreflow.modules.orderdetails.dto.OrderDetailsFullResponse;
import com.astraval.coreflow.modules.orderdetails.dto.OrderDetailsWithItems;
import com.astraval.coreflow.modules.orderdetails.dto.UnpaidOrderDto;
import com.astraval.coreflow.modules.orderdetails.repo.OrderDetailsRepository;
import com.astraval.coreflow.modules.orderitemsnapshot.OrderItemSnapshot;
import com.astraval.coreflow.modules.orderitemsnapshot.OrderItemSnapshotService;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetails;
import com.astraval.coreflow.modules.orderitemdetails.OrderItemDetailsRepository;
import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.repo.OrderSnapshotRepository;
import com.astraval.coreflow.modules.payments.service.PartnerBalanceService;

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

    @Autowired
    private PartnerBalanceService partnerBalanceService;
    
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

    public OrderDetailsFullResponse getOrderDetailsFullByOrderId(Long companyId, Long orderId) {
        OrderDetails orderDetails = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderItemDetails> orderItems = orderItemDetailsRepository.findByOrderId(orderId);

        OrderDetailsFullResponse response = new OrderDetailsFullResponse();
        response.setOrderId(orderDetails.getOrderId());
        response.setOrderNumber(orderDetails.getOrderNumber());
        response.setOrderDate(orderDetails.getOrderDate());

        if (orderDetails.getSellerCompany() != null) {
            response.setSellerCompanyId(orderDetails.getSellerCompany().getCompanyId());
            response.setSellerCompanyName(orderDetails.getSellerCompany().getCompanyName());
        }
        if (orderDetails.getBuyerCompany() != null) {
            response.setBuyerCompanyId(orderDetails.getBuyerCompany().getCompanyId());
            response.setBuyerCompanyName(orderDetails.getBuyerCompany().getCompanyName());
        }
        if (orderDetails.getCustomers() != null) {
            response.setCustomerId(orderDetails.getCustomers().getCustomerId());
            response.setCustomerName(orderDetails.getCustomers().getCustomerName());
            response.setCustomerDisplayName(orderDetails.getCustomers().getDisplayName());
        }
        if (orderDetails.getVendors() != null) {
            response.setVendorId(orderDetails.getVendors().getVendorId());
            response.setVendorName(orderDetails.getVendors().getVendorName());
            response.setVendorDisplayName(orderDetails.getVendors().getDisplayName());
        }

        response.setTaxAmount(orderDetails.getTaxAmount());
        response.setDiscountAmount(orderDetails.getDiscountAmount());
        response.setDeliveryCharge(orderDetails.getDeliveryCharge());
        response.setOrderAmount(orderDetails.getOrderAmount());
        response.setTotalAmount(orderDetails.getTotalAmount());
        response.setPaidAmount(orderDetails.getPaidAmount());
        response.setOrderStatus(orderDetails.getOrderStatus());
        response.setHasBill(orderDetails.getHasBill());
        response.setIsActive(orderDetails.getIsActive());
        response.setCreatedBy(orderDetails.getCreatedBy());
        response.setCreatedDt(orderDetails.getCreatedDt());
        response.setLastModifiedBy(orderDetails.getLastModifiedBy());
        response.setLastModifiedDt(orderDetails.getLastModifiedDt());

        List<OrderDetailsFullResponse.OrderItemDetailsFullResponse> itemResponses = orderItems.stream()
                .map(this::mapOrderItemDetails)
                .toList();
        response.setOrderItems(itemResponses);

        return response;
    }

    private OrderDetailsFullResponse.OrderItemDetailsFullResponse mapOrderItemDetails(OrderItemDetails orderItem) {
        OrderDetailsFullResponse.OrderItemDetailsFullResponse itemResponse =
                new OrderDetailsFullResponse.OrderItemDetailsFullResponse();

        itemResponse.setOrderItemId(orderItem.getOrderItemId());
        itemResponse.setOrderId(orderItem.getOrderId());
        if (orderItem.getItemId() != null) {
            itemResponse.setItemId(orderItem.getItemId().getItemId());
            itemResponse.setItemName(orderItem.getItemId().getItemName());
        }
        itemResponse.setItemDescription(orderItem.getItemDescription());
        itemResponse.setQuantity(orderItem.getQuantity());
        itemResponse.setUpdatedPrice(orderItem.getUpdatedPrice());
        itemResponse.setItemTotal(orderItem.getItemTotal());
        itemResponse.setReadyStatus(orderItem.getReadyStatus());
        itemResponse.setStatus(orderItem.getStatus());
        itemResponse.setIsActive(orderItem.getIsActive());

        return itemResponse;
    }
    
    @Transactional
    public void deleteOrder(Long companyId, Long orderId) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Long customerId = order.getCustomers() != null ? order.getCustomers().getCustomerId() : null;
        Long vendorId = order.getVendors() != null ? order.getVendors().getVendorId() : null;
        orderDetailsRepository.delete(order);
        partnerBalanceService.refreshDueAmounts(customerId, vendorId);
    }
    
    @Transactional
    public void deactivateOrder(Long companyId, Long orderId) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setIsActive(false);
        orderDetailsRepository.save(order);
        partnerBalanceService.refreshDueAmountsForOrder(order);
    }
    
    @Transactional
    public void activateOrder(Long companyId, Long orderId) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setIsActive(true);
        orderDetailsRepository.save(order);
        partnerBalanceService.refreshDueAmountsForOrder(order);
    }
    
    @Transactional
    public void updateOrderStatus(Long companyId, Long orderId, String newStatus) {
        OrderDetails order = orderDetailsRepository
                .findOrderForCompany(orderId, companyId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setOrderStatus(newStatus);
        orderDetailsRepository.save(order);
        partnerBalanceService.refreshDueAmountsForOrder(order);
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
        partnerBalanceService.refreshDueAmountsForOrder(order);
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

    public List<UnpaidOrderDto> getUnpaidOrdersBySellerCompanyIdAndCustomerId(Long sellerCompanyId, Long customerId) {
        String orderStatus = OrderStatus.getOrderInvoiced();
        return orderDetailsRepository.findUnpaidOrdersBySellerCompanyIdAndCustomerId(sellerCompanyId, customerId, orderStatus);
    }
}
