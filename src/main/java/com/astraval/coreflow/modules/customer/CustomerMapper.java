package com.astraval.coreflow.modules.customer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.global.model.Address;
import com.astraval.coreflow.global.model.Customers;
import com.astraval.coreflow.modules.customer.dto.AddressDto;
import com.astraval.coreflow.modules.customer.dto.CreateCustomerRequest;
import com.astraval.coreflow.modules.customer.projection.CustomerProjection;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);
    
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "customerCompany", ignore = true)
    @Mapping(target = "acceptedInvitationId", ignore = true)
    @Mapping(target = "billingAddrId", ignore = true)
    @Mapping(target = "shippingAddrId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customers toCustomer(CreateCustomerRequest request);
    
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDt", ignore = true)
    Address toAddress(AddressDto addressDto);
    
    CustomerProjection toProjection(Customers customer);
}