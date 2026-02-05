package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateVendorItemDto {
    private BigDecimal purchasePrice;
    private String purchaseDescription;
}
