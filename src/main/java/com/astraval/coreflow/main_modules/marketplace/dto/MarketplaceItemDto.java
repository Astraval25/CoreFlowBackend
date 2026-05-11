package com.astraval.coreflow.main_modules.marketplace.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.main_modules.items.ItemType;
import com.astraval.coreflow.main_modules.items.UnitType;

public record MarketplaceItemDto(
        Long itemId,
        String itemName,
        ItemType itemType,
        UnitType unit,
        String salesDescription,
        BigDecimal salesPrice,
        BigDecimal taxRate,
        String hsnCode,
        String fsId) {
}
