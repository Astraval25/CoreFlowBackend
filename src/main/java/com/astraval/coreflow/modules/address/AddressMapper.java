package com.astraval.coreflow.modules.address;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.address.projection.AddressProjection;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);
    
    AddressProjection toProjection(Address address);
}