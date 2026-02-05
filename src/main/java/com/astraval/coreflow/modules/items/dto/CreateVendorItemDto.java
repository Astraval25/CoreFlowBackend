package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVendorItemDto {

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Purchase price is required")
    private BigDecimal purchasePrice;

    private String purchaseDescription;
}
