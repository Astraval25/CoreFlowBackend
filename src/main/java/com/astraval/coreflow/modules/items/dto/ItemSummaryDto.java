package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

public record ItemSummaryDto(
    Long itemId,
    String itemName,
    String itemDisplayName,
    ItemType itemType,
    UnitType unit,
    BigDecimal salesPrice,
    Long preferredCustomerId,
    String preferredCustomerName,
    BigDecimal purchasePrice,
    Long preferredVendorId,
    String preferredVendorName,
    Boolean isActive) {
}