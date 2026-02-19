package com.astraval.coreflow.modules.orderdetails.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrderDetailsFullResponse {
    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;

    private Long sellerCompanyId;
    private String sellerCompanyName;
    private Long buyerCompanyId;
    private String buyerCompanyName;

    private Long customerId;
    private String customerName;
    private String customerDisplayName;

    private Long vendorId;
    private String vendorName;
    private String vendorDisplayName;

    private Double taxAmount;
    private Double discountAmount;
    private Double deliveryCharge;
    private Double orderAmount;
    private Double totalAmount;
    private Double paidAmount;
    private String orderStatus;
    private Boolean hasBill;
    private Boolean isActive;

    private Long createdBy;
    private LocalDateTime createdDt;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedDt;

    private List<OrderItemDetailsFullResponse> orderItems;

    @Data
    public static class OrderItemDetailsFullResponse {
        private Long orderItemId;
        private Long orderId;
        private Long itemId;
        private String itemName;
        private String itemDescription;
        private Double quantity;
        private Double updatedPrice;
        private Double itemTotal;
        private Double readyStatus;
        private String status;
        private Boolean isActive;
    }
}
