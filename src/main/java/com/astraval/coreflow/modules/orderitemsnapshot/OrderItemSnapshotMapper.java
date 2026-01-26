package com.astraval.coreflow.modules.orderitemsnapshot;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.items.model.Items;
import com.astraval.coreflow.modules.orderitemsnapshot.dto.CreateOrderItem;
import com.astraval.coreflow.modules.orderitemsnapshot.dto.OrderItemResponse;

@Mapper(componentModel = "spring")
public interface OrderItemSnapshotMapper {
    
    OrderItemSnapshotMapper INSTANCE = Mappers.getMapper(OrderItemSnapshotMapper.class);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "orderItemId", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "status", constant = "OPEN")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDt", ignore = true)
    OrderItemSnapshot toOrderItemSnapshot(CreateOrderItem createOrderItem);
    
    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "itemId.itemName", target = "itemName")
    OrderItemResponse toOrderItemResponse(OrderItemSnapshot orderItemSnapshot);

    default Long map(Items items) {
        return items != null ? items.getItemId() : null;
    }
}
