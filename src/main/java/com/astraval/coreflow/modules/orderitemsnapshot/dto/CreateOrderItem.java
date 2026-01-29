package com.astraval.coreflow.modules.orderitemsnapshot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateOrderItem {
  @NotNull(message = "Item is required")
  private Long itemId;
  
  private String itemDescription;

  @Positive(message = "Quantity must be positive value")
  @NotNull(message = "Quantity is required")
  private Double quantity;

  @Positive(message = "Update Price must be positive value")
  @NotNull(message = "Updated Price is required")
  private Double updatedPrice;
}