package com.astraval.coreflow.modules.ordersnapshot.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.astraval.coreflow.modules.orderitemsnapshot.dto.OrderItemResponse;

import lombok.Data;

@Data
public class OrderSnapshotDto {
  private Long orderId;
  private String orderNumber;
  private LocalDateTime orderDate;
  private String sellerCompanyName;
  private String buyerCompanyName;
  private Double orderAmount;
  private Double taxAmount;
  private Double discountAmount;
  private Double deliveryCharge;
  private String orderStatus;
  private List<OrderItemResponse> orderItems;
}