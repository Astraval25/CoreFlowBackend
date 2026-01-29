package com.astraval.coreflow.modules.orderitemsnapshot.dto;

import lombok.Data;

@Data
public class OrderItemResponse {
  private Long orderItemId;
  private Long itemId;
  private String itemName;
  private String itemDescription;
  private Integer quantity;
  private Double basePrice;
  private Double updatedPrice;
  private String unitOfMeasure;
  private String status;
}