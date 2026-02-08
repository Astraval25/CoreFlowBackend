package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

public record ItemSummaryDto(
    Long itemId,
    String itemName,
    ItemType itemType,
    UnitType unit,
    BigDecimal baseSalesPrice,
    BigDecimal basePurchasePrice,
    Boolean isActive,
    Boolean isSellable,
    Boolean isPurchasable,
    String fsId) {
}
