package com.astraval.coreflow.modules.address;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.address.dto.CreateUpdateAddressDto;
import com.astraval.coreflow.modules.address.projection.AddressProjection;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface AddressMapper {
    
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);
    

    AddressProjection toProjection(Address address);

    Address toAddress(CreateUpdateAddressDto dto);
}