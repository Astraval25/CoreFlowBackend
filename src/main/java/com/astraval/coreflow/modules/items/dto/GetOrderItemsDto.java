package com.astraval.coreflow.modules.items.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOrderItemsDto {

    private Long itemId;
    private String itemName;
    private Double price; // salesPrice for case1, purchasePrice for case2
    private String description; // salesDescription for case1, purchaseDescription for case2
    private String hsnCode;
    private Double taxRate;
    private String unit;
    
}
