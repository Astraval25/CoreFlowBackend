package com.astraval.coreflow.modules.orderitemdetails.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderItem {
  @NotBlank(message = "Item is required")
  private Long itemId;
  @NotBlank(message = "Quantity is required")
  private Integer quantity;
  
  private Double basePrice;
  
  @NotBlank(message = "Updated Price is required")
  private Double updatedPrice;
}