package com.astraval.coreflow.modules.orderdetails.dto;

  
import java.util.List;

import com.astraval.coreflow.modules.orderitemdetails.dto.CreateOrderItem;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrder {

  @NotNull(message = "Customer id is required")
  private Long customerId;
  
  private Double taxAmount;
  private Double discountAmount;
  
  @NotNull(message = "Delivery charge is required")
  private Double deliveryCharge;
  
  @NotNull(message = "Add atleast one item to create order is required")
  private List<CreateOrderItem> orderItems;
  
  @NotNull(message = "Has bill is required")
  private boolean hasBill;
}
