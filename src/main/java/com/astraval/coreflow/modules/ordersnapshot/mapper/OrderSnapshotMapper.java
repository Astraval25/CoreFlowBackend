package com.astraval.coreflow.modules.ordersnapshot.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.astraval.coreflow.modules.ordersnapshot.OrderSnapshot;
import com.astraval.coreflow.modules.ordersnapshot.dto.CreatePurchaseOrder;
import com.astraval.coreflow.modules.ordersnapshot.dto.CreateSalesOrder;
import com.astraval.coreflow.modules.ordersnapshot.dto.SalesOrderResponse;

@Mapper(componentModel = "spring")
public interface OrderSnapshotMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    // @Mapping(target = "orderStatus", constant = "OPEN")
    @Mapping(target = "sellerCompany", ignore = true)
    @Mapping(target = "buyerCompany", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDt", ignore = true)
    OrderSnapshot toOrderSnapshot(CreateSalesOrder createOrder);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    // @Mapping(target = "orderStatus", constant = "OPEN")
    @Mapping(target = "sellerCompany", ignore = true)
    @Mapping(target = "buyerCompany", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDt", ignore = true)
    OrderSnapshot toPurchaseOrderSnapshot(CreatePurchaseOrder createOrder);

    @Mapping(source = "sellerCompany.companyName", target = "sellerCompanyName")
    @Mapping(source = "buyerCompany.companyName", target = "buyerCompanyName")
    @Mapping(target = "orderItems", ignore = true)
    SalesOrderResponse toOrderResponse(OrderSnapshot orderSnapshot);
}
