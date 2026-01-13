package com.astraval.coreflow.modules.orderitemdetails.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderItem {
  @NotNull(message = "Item is required")
  private Long itemId;
  
  private String itemDescription;

  @NotNull(message = "Quantity is required")
  private Double quantity;

  @NotNull(message = "Updated Price is required")
  private Double updatedPrice;
}