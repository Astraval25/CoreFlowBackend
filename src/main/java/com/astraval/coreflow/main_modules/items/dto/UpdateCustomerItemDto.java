package com.astraval.coreflow.main_modules.items.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateCustomerItemDto {
    private BigDecimal salesPrice;
    private String salesDescription;
}
