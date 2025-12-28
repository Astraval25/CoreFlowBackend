package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

public record ItemSummaryDto(
    Long itemId,
    String itemName,
    String itemCode,
    String category,
    UnitType unit,
    BigDecimal sellingPrice,
    ItemType itemType,
    Integer stockQuantity,
    Boolean isActive) {
}