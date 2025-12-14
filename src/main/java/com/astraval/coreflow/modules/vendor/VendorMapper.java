package com.astraval.coreflow.modules.vendor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.address.Address;
import com.astraval.coreflow.modules.customer.dto.AddressDto;
import com.astraval.coreflow.modules.vendor.dto.CreateVendorRequest;
import com.astraval.coreflow.modules.vendor.projection.VendorProjection;

@Mapper(componentModel = "spring")
public interface VendorMapper {
    
    VendorMapper INSTANCE = Mappers.getMapper(VendorMapper.class);
    
    @Mapping(target = "vendorId", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "vendorCompany", ignore = true)
    @Mapping(target = "acceptedInvitationId", ignore = true)
    @Mapping(target = "billingAddrId", ignore = true)
    @Mapping(target = "shippingAddrId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vendors toVendor(CreateVendorRequest request);
    
    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDt", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "modifiedDt", ignore = true)
    Address toAddress(AddressDto addressDto);
    
    VendorProjection toProjection(Vendors vendor);
}