package com.astraval.coreflow.main_modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.main_modules.items.ItemType;
import com.astraval.coreflow.main_modules.items.UnitType;

public record VendorItemSummaryDto(
    Long itemId,
    String itemName,
    ItemType itemType,
    UnitType unit,
    BigDecimal purchasePrice,
    String purchaseDescription,
    String hsnCode,
    BigDecimal taxRate,
    Boolean isActive,
    String source,
        String fsId,
        Boolean editable
) {
}
