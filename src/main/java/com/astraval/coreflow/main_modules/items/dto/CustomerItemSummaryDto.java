package com.astraval.coreflow.main_modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.main_modules.items.ItemType;
import com.astraval.coreflow.main_modules.items.UnitType;

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
    String source,
    String fsId
) {
}
