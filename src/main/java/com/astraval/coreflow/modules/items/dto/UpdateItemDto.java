package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

import lombok.Data;

@Data
public class UpdateItemDto {

    private String itemName;
    private String itemDisplayName;
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
    private BigDecimal stockQuantity;
}