package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUpdateItemDto {

    @NotBlank(message = "Item name is required")
    private String itemName;

    private String itemCode;

    private String description;

    private String category;

    private UnitType unit;

    private BigDecimal sellingPrice;

    private ItemType itemType;

    private String hsnCode;

    private BigDecimal taxRate;

    private Integer stockQuantity;
}