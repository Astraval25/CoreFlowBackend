package com.astraval.coreflow.modules.orderdetails.dto;

  
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrder {

  @NotNull
  private Long customId;
  
  private Double taxAmount;
  private Double discountAmount;
  
  @NotNull
  private Double deliveryCharge;
  
  // @NotBlank(message = "Add atleast one item to create order is required")
  // private List<CreateOrderItem> createOrderItems;
}
