package com.astraval.coreflow.employee_module.portaluser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePortalUserDto {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
