package com.astraval.coreflow.modules.companies;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.companies.dto.AdminCompaniesResponseDto;
import com.astraval.coreflow.modules.usercompmap.UserCompanyMap;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    
    CompanyMapper INSTANCE = Mappers.getMapper(CompanyMapper.class);
    
    @Mapping(source = "company.companyId", target = "companyId")
    @Mapping(source = "company.companyName", target = "companyName")
    @Mapping(source = "user.userName", target = "userName")
    AdminCompaniesResponseDto toAdminCompanyResponseDto(UserCompanyMap userCompanyMap);
}
