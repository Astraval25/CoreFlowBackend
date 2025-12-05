package com.astraval.coreflow.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.dto.response.AdminCompanyiesResponseDto;
import com.astraval.coreflow.model.Companies;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    
    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);
    
    @Mapping(source = "companyname", target = "companyName")
    AdminCompanyiesResponseDto toAdminCompanyResponseDto(Companies company);
}
