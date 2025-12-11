package com.astraval.coreflow.modules.auth;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.global.model.Companies;
import com.astraval.coreflow.global.model.Role;
import com.astraval.coreflow.global.model.User;
import com.astraval.coreflow.modules.auth.dto.LoginRequest;
import com.astraval.coreflow.modules.auth.dto.LoginResponse;
import com.astraval.coreflow.modules.auth.dto.RegisterRequest;
import com.astraval.coreflow.modules.auth.dto.RegisterResponse;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    // Map User + Role to LoginResponse
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "role.roleCode", target = "roleCode")
    @Mapping(source = "role.landingUrl", target = "landingUrl")
    @Mapping(source = "user.defaultCompany.companyId", target = "companyId")
    @Mapping(source = "user.defaultCompany.companyName", target = "companyName")
    LoginResponse toLoginResponse(User user, Role role, String token, String refreshToken);

    // Optional: Map LoginRequest → User (useful for registration)
    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    User toUser(LoginRequest loginRequest);

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

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.userName", target = "userName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "company.companyId", target = "companyId")
    @Mapping(source = "company.companyName", target = "companyName")
    @Mapping(source = "company.industry", target = "industry")
    @Mapping(source = "role.roleCode", target = "roleCode")
    RegisterResponse toRegisterResponse(User user, Companies company, Role role, String accessToken,
            String refreshToken);
}
