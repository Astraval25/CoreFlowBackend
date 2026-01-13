package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateItemDto {

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotBlank(message = "Item display name is required")
    private String itemDisplayName;

    @NotNull(message = "Item type is required")
    private ItemType itemType;

    private UnitType unit;

    private String salesDescription;

    private BigDecimal salesPrice;

    private Long preferredCustomerId;

    private String purchaseDescription;

    private BigDecimal purchasePrice;

    private Long preferredVendorId;

    private String hsnCode;

    private BigDecimal taxRate;
}