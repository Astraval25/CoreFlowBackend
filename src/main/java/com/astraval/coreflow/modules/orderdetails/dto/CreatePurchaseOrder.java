package com.astraval.coreflow.modules.orderdetails.dto;

  
import java.util.List;

import com.astraval.coreflow.modules.orderitemdetails.dto.CreateOrderItem;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreatePurchaseOrder {

  @NotNull(message = "Customer id is required")
  private Long customerId;
  
  @PositiveOrZero(message = "Tax amount must be positive or zero")
  private Double taxAmount;
  
  @PositiveOrZero(message = "Discount amount must be positive or zero")
  private Double discountAmount;
  
  @NotNull(message = "Delivery charge is required")
  @PositiveOrZero(message = "Delivery charge must be positive or zero")
  private Double deliveryCharge;
  
  @NotNull(message = "Add atleast one item to create order is required")
  @Valid
  private List<CreateOrderItem> orderItems;
  
  @NotNull(message = "Has bill is required")
  private boolean hasBill;
}
