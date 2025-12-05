package com.astraval.coreflow.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.astraval.coreflow.dto.request.LoginRequest;
import com.astraval.coreflow.dto.response.LoginResponse;
import com.astraval.coreflow.model.Role;
import com.astraval.coreflow.model.User;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

    // Map User + Role to LoginResponse
    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "role.roleCode", target = "roleCode")
    @Mapping(source = "role.landingUrl", target = "landingUrl")
    @Mapping(source = "user.defaultCompany.companyId", target = "companyId")
    @Mapping(source = "user.defaultCompany.companyname", target = "companyName")
    LoginResponse toLoginResponse(User user, Role role, String token, String refreshToken);

    // Optional: Map LoginRequest → User (useful for registration)
    @BeanMapping(ignoreByDefault  = true)
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    User toUser(LoginRequest loginRequest);
}
