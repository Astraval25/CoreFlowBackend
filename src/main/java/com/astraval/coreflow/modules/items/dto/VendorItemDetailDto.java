package com.astraval.coreflow.modules.items.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.astraval.coreflow.modules.items.ItemType;
import com.astraval.coreflow.modules.items.UnitType;

import lombok.Data;

@Data
public class VendorItemDetailDto {
    private Long itemId;
    private String itemName;
    private ItemType itemType;
    private UnitType unit;
    private BigDecimal purchasePrice;
    private String purchaseDescription;
    private String hsnCode;
    private BigDecimal taxRate;
    private Boolean isActive;
    private Long createdBy;
    private LocalDateTime createdDt;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedDt;
    private String itemImage;
}
