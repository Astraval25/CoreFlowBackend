package com.astraval.coreflow.modules.orderitemdetails;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.orderitemdetails.dto.CreateOrderItem;
import com.astraval.coreflow.modules.orderitemdetails.dto.OrderItemResponse;

@Mapper(componentModel = "spring")
public interface OrderItemDetailsMapper {
    
    OrderItemDetailsMapper INSTANCE = Mappers.getMapper(OrderItemDetailsMapper.class);
    
    @Mapping(target = "orderItemId", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDt", ignore = true)
    OrderItemDetails toOrderItemDetails(CreateOrderItem createOrderItem);
    
    @Mapping(source = "itemId.itemName", target = "itemName")
    OrderItemResponse toOrderItemResponse(OrderItemDetails orderItemDetails);
}
