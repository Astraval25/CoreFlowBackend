package com.astraval.coreflow.modules.items;

import org.springframework.stereotype.Component;

import com.astraval.coreflow.modules.items.dto.CreateItemDto;
import com.astraval.coreflow.modules.items.model.Items;

@Component
public class ItemMapper {

    public void mapDtoToEntity(CreateItemDto dto, Items item) {
        item.setItemName(dto.getItemName());
        item.setItemType(dto.getItemType());
        item.setUnit(dto.getUnit());
        item.setSalesDescription(dto.getSalesDescription());
        item.setBaseSalesPrice(dto.getBaseSalesPrice());
        item.setPurchaseDescription(dto.getPurchaseDescription());
        item.setBasePurchasePrice(dto.getBasePurchasePrice());
        item.setHsnCode(dto.getHsnCode());
        item.setTaxRate(dto.getTaxRate());
    }

    public void mapUpdateDtoToEntity(com.astraval.coreflow.modules.items.dto.UpdateItemDto dto, Items item) {
        if (dto.getItemName() != null) item.setItemName(dto.getItemName());
        if (dto.getItemType() != null) item.setItemType(dto.getItemType());
        if (dto.getUnit() != null) item.setUnit(dto.getUnit());
        if (dto.getSalesDescription() != null) item.setSalesDescription(dto.getSalesDescription());
        if (dto.getBaseSalesPrice() != null) item.setBaseSalesPrice(dto.getBaseSalesPrice());
        if (dto.getPurchaseDescription() != null) item.setPurchaseDescription(dto.getPurchaseDescription());
        if (dto.getBasePurchasePrice() != null) item.setBasePurchasePrice(dto.getBasePurchasePrice());
        if (dto.getHsnCode() != null) item.setHsnCode(dto.getHsnCode());
        if (dto.getTaxRate() != null) item.setTaxRate(dto.getTaxRate());
    }
}
