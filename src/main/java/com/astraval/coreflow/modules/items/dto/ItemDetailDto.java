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
    private String itemDisplayName;
    private ItemType itemType;
    private UnitType unit;
    private BigDecimal salesPrice;
    private String salesDescription;
    private String preferredCustomer;
    private Long preferredCustomerId;
    private String preferredCustomerDisplayName;
    private BigDecimal purchasePrice;
    private String purchaseDescription;
    private Long preferredVendorId;
    private String preferredVendorDisplayName;
    private String hsnCode;
    private BigDecimal taxRate;
    private Boolean isActive;
    private Long createdBy;
    private LocalDateTime createdDt;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedDt;
    private String itemImage;
}