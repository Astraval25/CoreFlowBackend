package com.astraval.coreflow.main_modules.items.dto;

import java.math.BigDecimal;

import com.astraval.coreflow.main_modules.items.ItemType;
import com.astraval.coreflow.main_modules.items.UnitType;

import lombok.Data;

@Data
public class UpdateItemDto {

    private String itemName;

    private ItemType itemType;
    private UnitType unit;
    private String salesDescription;
    private BigDecimal baseSalesPrice;
    private String purchaseDescription;
    private BigDecimal basePurchasePrice;
    private String hsnCode;
    private BigDecimal taxRate;
}
