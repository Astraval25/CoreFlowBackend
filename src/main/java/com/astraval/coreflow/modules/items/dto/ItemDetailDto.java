package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

import lombok.Data;

@Data
public class ItemDetailDto {
    private Long itemId;
    private String itemName;

    private ItemType itemType;
    private UnitType unit;
    private BigDecimal baseSalesPrice;
    private String salesDescription;
    private BigDecimal basePurchasePrice;
    private String purchaseDescription;
    private String hsnCode;
    private BigDecimal taxRate;
    private Boolean isActive;
    private Boolean isSellable;
    private Boolean isPurchasable;
    private Long createdBy;
    private LocalDateTime createdDt;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedDt;
    private String itemImage;
}
