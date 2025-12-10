package com.astraval.coreflow.modules.companies.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.global.model.Companies;
import com.astraval.coreflow.modules.companies.dto.AdminCompaniesResponseDto;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    
    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);
    
    @Mapping(source = "companyName", target = "companyName")
    AdminCompaniesResponseDto toAdminCompanyResponseDto(Companies company);
}
