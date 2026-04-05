package com.astraval.coreflow.main_modules.orderdetails.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.astraval.coreflow.main_modules.orderitemdetails.dto.CreateOrderItem;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateSalesOrder {

    @NotNull(message = "Customer id is required")
    private Long customerId;
    
    @PositiveOrZero(message = "Tax amount must be positive or zero")
    private Double taxAmount;
    
    @PositiveOrZero(message = "Discount amount must be positive or zero")
    private Double discountAmount;

    private LocalDateTime orderDate;
    
    @NotNull(message = "Delivery charge is required")
    @PositiveOrZero(message = "Delivery charge must be positive or zero")
    private Double deliveryCharge;
    
    @NotNull(message = "Add atleast one item to update order is required")
    @Valid
    private List<CreateOrderItem> orderItems;
    
    @NotNull(message = "Has bill is required")
    private boolean hasBill;
}
