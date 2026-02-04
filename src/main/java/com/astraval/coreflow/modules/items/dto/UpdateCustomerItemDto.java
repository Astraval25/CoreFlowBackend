package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UpdateCustomerItemDto {
    private BigDecimal salesPrice;
    private String salesDescription;
}
