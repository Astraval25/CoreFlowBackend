package com.astraval.coreflow.modules.orderdetails.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.astraval.coreflow.modules.orderdetails.OrderDetails;
import com.astraval.coreflow.modules.orderdetails.dto.CreatePurchaseOrder;
import com.astraval.coreflow.modules.orderdetails.dto.CreateSalesOrder;
import com.astraval.coreflow.modules.orderdetails.dto.SalesOrderResponse;

@Mapper(componentModel = "spring")
public interface OrderDetailsMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "orderStatus", constant = "OPEN")
    @Mapping(target = "sellerCompany", ignore = true)
    @Mapping(target = "buyerCompany", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDt", ignore = true)
    OrderDetails toOrderDetails(CreateSalesOrder createOrder);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "orderStatus", constant = "OPEN")
    @Mapping(target = "sellerCompany", ignore = true)
    @Mapping(target = "buyerCompany", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDt", ignore = true)
    OrderDetails toPurchaseOrderDetails(CreatePurchaseOrder createOrder);

    @Mapping(source = "sellerCompany.companyName", target = "sellerCompanyName")
    @Mapping(source = "buyerCompany.companyName", target = "buyerCompanyName")
    @Mapping(target = "orderItems", ignore = true)
    SalesOrderResponse toOrderResponse(OrderDetails orderDetails);
}
