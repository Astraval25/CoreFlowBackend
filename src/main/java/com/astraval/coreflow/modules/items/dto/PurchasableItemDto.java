package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchasableItemDto {
    private Long itemId;
    private String itemName;
    private String purchaseDescription;
    private BigDecimal basePurchasePrice;
    private BigDecimal taxRate;
    private String hsnCode;
}
