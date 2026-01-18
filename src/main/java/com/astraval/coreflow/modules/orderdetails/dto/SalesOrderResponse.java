package com.astraval.coreflow.modules.orderdetails.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.astraval.coreflow.modules.orderitemdetails.dto.OrderItemResponse;

import lombok.Data;

@Data
public class SalesOrderResponse {
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