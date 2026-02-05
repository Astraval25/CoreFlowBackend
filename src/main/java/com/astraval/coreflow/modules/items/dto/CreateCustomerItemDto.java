package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCustomerItemDto {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Sales price is required")
    private BigDecimal salesPrice;

    private String salesDescription;
}
