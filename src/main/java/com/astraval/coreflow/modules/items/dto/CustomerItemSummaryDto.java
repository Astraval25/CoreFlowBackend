package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

public record CustomerItemSummaryDto(
    Long itemId,
    String itemName,
    ItemType itemType,
    UnitType unit,
    BigDecimal salesPrice,
    String salesDescription,
    String hsnCode,
    BigDecimal taxRate,
    Boolean isActive,
    String source
) {
}
