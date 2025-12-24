package com.astraval.coreflow.modules.Auth;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.modules.Auth.dto.LoginResponse;
import com.astraval.coreflow.modules.Auth.dto.RegisterRequest;
import com.astraval.coreflow.modules.companies.Companies;
import com.astraval.coreflow.modules.role.Role;
import com.astraval.coreflow.modules.user.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    // Map User + Role to LoginResponse
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "role.roleCode", target = "roleCode")
    @Mapping(source = "role.landingUrl", target = "landingUrl")
    @Mapping(source = "user.defaultCompany.companyId", target = "companyId")
    @Mapping(source = "user.defaultCompany.companyName", target = "companyName")
    @Mapping(target = "companyIds", ignore = true)
    LoginResponse toLoginResponse(User user, Role role, String token, String refreshToken);

    // For registration
    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "email", target = "email")
    User toUser(RegisterRequest registerRequest);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "companyName", target = "companyName")
    @Mapping(source = "industry", target = "industry")
    Companies toCompany(RegisterRequest registerRequest);

}
