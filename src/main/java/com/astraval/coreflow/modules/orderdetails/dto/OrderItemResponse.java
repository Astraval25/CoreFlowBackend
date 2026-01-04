package com.astraval.coreflow.modules.orderdetails.dto;

import lombok.Data;

@Data
public class OrderItemResponse {
  private Long orderItemId;
  private String itemName;
  private Integer quantity;
  private Double basePrice;
  private Double updatedPrice;
  private String unitOfMeasure;
  private String status;
}