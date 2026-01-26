package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellableItemDto {
    private Long itemId;
    private String itemName;
    private String salesDescription;
    private BigDecimal salesPrice;
    private String preferredCustomer;
    private BigDecimal taxRate;
    private String hsnCode;
}