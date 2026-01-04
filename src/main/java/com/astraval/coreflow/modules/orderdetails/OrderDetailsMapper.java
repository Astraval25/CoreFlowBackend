package com.astraval.coreflow.modules.orderdetails;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.orderdetails.dto.CreateOrder;
import com.astraval.coreflow.modules.orderdetails.dto.OrderResponse;

@Mapper(componentModel = "spring")
public interface OrderDetailsMapper {

    OrderDetailsMapper INSTANCE = Mappers.getMapper(OrderDetailsMapper.class);

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "orderStatus", constant = "PENDING")
    @Mapping(target = "sellerCompany", ignore = true)
    @Mapping(target = "buyerCompany", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDt", ignore = true)
    OrderDetails toOrderDetails(CreateOrder createOrder);

    @Mapping(source = "sellerCompany.companyName", target = "sellerCompanyName")
    @Mapping(source = "buyerCompany.companyName", target = "buyerCompanyName")
    @Mapping(target = "orderItems", ignore = true)
    OrderResponse toOrderResponse(OrderDetails orderDetails);
}
